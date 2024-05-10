package dev.roanoke.rib.quests.types

import com.cobblemon.mod.common.api.apricorn.Apricorn
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import net.minecraft.text.Text
import dev.roanoke.rib.quests.Quest
import dev.roanoke.rib.quests.QuestGroup
import dev.roanoke.rib.quests.QuestProvider
import java.util.UUID

class HarvestApricornQuest(
    name: String = "Harvest Some Apricorns",
    uuid: UUID = UUID.randomUUID(),
    provider: QuestProvider,
    group: QuestGroup,
    var apricorn: Apricorn = Apricorn.RED,
    var amount: Int = 3,
    var progress: Int = 0
    ) :
    Quest(name, uuid, provider, group) {

    companion object : Quest.QuestFactory {
        override fun fromState(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {
            val name = json.get("name").asString
            val uuid = UUID.fromString(json.get("uuid").asString)

            val apricornString = json.get("apricorn").asString

            var apricorn: Apricorn = Apricorn.BLACK
            try {
                apricorn = Apricorn.valueOf(apricornString.uppercase())
            } catch (e: IllegalArgumentException) {
                Rib.LOGGER.info("Failed to convert Apricorn String in Quest (${apricornString}) to an Apricorn Enum")
            }

            val amount = json.get("amount").asInt

            // anything that is stateful goes here

            val progress = state.get("progress")?.asInt ?: 0

            return HarvestApricornQuest(name, uuid, provider, group, apricorn, amount, progress)
        }
    }

    override fun getState(): JsonObject {
        return JsonObject().apply {
            addProperty("progress", progress)
        }
    }

    override fun applyState(state: JsonObject) {
        progress = state.get("progress")?.asInt ?: progress
    }

    init {
        CobblemonEvents.APRICORN_HARVESTED.subscribe {
            if (!isActive()) {
                return@subscribe
            }

            if (group.includesPlayer(it.player)) {
                if (it.apricorn == this.apricorn) {
                    progress += 1
                    this.notifyProgress()

                    if (completed()) {
                        this.notifyCompletion()
                    }
                }
            }
        }
    }

    override fun completed(): Boolean {
        return (progress >= amount)
    }

    override fun taskMessage(): Text {
        return Text.literal("Harvest ${amount}x ${apricorn.name.lowercase().capitalize()} Apricorns")
    }

    override fun progressMessage(): Text {
        return Text.literal("${progress} / ${amount}")
    }

    override fun saveState(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", "HarvestApricornQuest")
        jsonObject.addProperty("name", name)
        jsonObject.addProperty("uuid", id.toString())
        jsonObject.addProperty("apricorn", apricorn.toString())
        jsonObject.addProperty("amount", amount)
        jsonObject.addProperty("progress", progress)
        return jsonObject
    }
}