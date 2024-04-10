package dev.roanoke.rib.quests.types

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.google.gson.JsonObject
import dev.roanoke.rib.cobblemon.PokeMatch
import net.minecraft.text.Text
import dev.roanoke.rib.quests.Quest
import dev.roanoke.rib.quests.QuestGroup
import dev.roanoke.rib.quests.QuestProvider
import java.util.*

class CatchPokemonQuest(name: String = "Default Catch Pokemon Quest Title",
                        uuid: UUID = UUID.randomUUID(),
                        provider: QuestProvider,
                        group: QuestGroup,
                        var pokeMatch: PokeMatch = PokeMatch(),
                        var taskMessage: String = "Catch a Pokemon!",
                        var amount: Int = 1,
                        var progress: Int = 0
) :
    Quest(name, uuid, provider, group) {

    companion object : Quest.QuestFactory {
        override fun fromState(json: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {
            val name = json.get("name").asString
            val uuid = UUID.fromString(json.get("uuid").asString)

            val pokeMatch = PokeMatch.fromJson(json.get("pokeMatch").asJsonObject)
            val taskMessage = json.get("taskMessage").asString

            val amount = json.get("amount").asInt
            val progress = json.get("progress").asInt

            return CatchPokemonQuest(name, uuid, provider, group, pokeMatch, taskMessage, amount, progress)
        }
    }

    init {
        CobblemonEvents.POKEMON_CAPTURED.subscribe {
            if (!isActive()) {
                return@subscribe
            }

            if (group.includesPlayer(it.player) && pokeMatch.matches(it.pokemon)) {
                progress += 1
                this.notifyProgress()
                if (completed()) {
                    this.notifyCompletion()
                }
            }
        }
    }

    override fun completed(): Boolean {
        return progress >= amount
    }

    override fun taskMessage(): Text {
        return Text.literal(taskMessage)
    }

    override fun progressMessage(): Text {
        return Text.literal("${progress} / ${amount}")
    }

    override fun saveState(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", "CatchPokemonQuest")
        jsonObject.addProperty("name", name)
        jsonObject.addProperty("uuid", id.toString())

        jsonObject.add("pokeMatch", pokeMatch.toJson())
        jsonObject.addProperty("taskMessage", taskMessage)

        jsonObject.addProperty("amount", amount)
        jsonObject.addProperty("progress", progress)
        return jsonObject
    }
}