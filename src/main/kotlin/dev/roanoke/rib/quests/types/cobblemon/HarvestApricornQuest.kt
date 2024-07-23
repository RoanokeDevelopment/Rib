package dev.roanoke.rib.quests.types.cobblemon

import com.cobblemon.mod.common.api.apricorn.Apricorn
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import net.minecraft.text.Text
import dev.roanoke.rib.quests.Quest
import dev.roanoke.rib.quests.QuestFactory
import dev.roanoke.rib.quests.QuestGroup
import dev.roanoke.rib.quests.QuestProvider
import dev.roanoke.rib.rewards.RewardList
import dev.roanoke.rib.utils.ItemBuilder
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.server.network.ServerPlayerEntity
import java.util.UUID

class HarvestApricornQuest(
    name: String = "Harvest Some Apricorns",
    id: String = UUID.randomUUID().toString(),
    provider: QuestProvider,
    group: QuestGroup,
    var apricorn: Apricorn = Apricorn.RED,
    var amount: Int = 3,
    var progress: Int = 0
    ) :
    Quest(name, id, provider, group) {

    companion object : QuestFactory {
        override fun fromJson(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {
            val name = json.get("name")?.asString ?: "Default Harvest Apricorn Quest Title"
            val id = json.get("id")?.asString ?: UUID.randomUUID().toString()

            val apricornString = json.get("apricorn")?.asString ?: "BLACK"

            var apricorn: Apricorn = Apricorn.BLACK
            try {
                apricorn = Apricorn.valueOf(apricornString.uppercase())
            } catch (e: IllegalArgumentException) {
                Rib.LOGGER.info("Failed to convert Apricorn String in Quest (${apricornString}) to an Apricorn Enum")
            }

            val amount = json.get("amount")?.asInt ?: 3
            val rRewards = RewardList.fromJson(json.get("rewards"))

            // anything that is stateful goes here

            val rRewardsClaimed = state.get("rewardsClaimed")?.asBoolean ?: false

            val progress = state.get("progress")?.asInt ?: 0

            return HarvestApricornQuest(name, id, provider, group, apricorn, amount, progress).apply {
                rewards = rRewards;
                rewardsClaimed = rRewardsClaimed
            }
        }
    }

    override fun getState(): JsonObject {
        return JsonObject().apply {
            addProperty("progress", progress)
            addProperty("rewardsClaimed", rewardsClaimed)
        }
    }

    override fun applyState(state: JsonObject) {
        progress = state.get("progress")?.asInt ?: progress
        rewardsClaimed = state.get("rewardsClaimed")?.asBoolean ?: rewardsClaimed
    }

    override fun getButton(player: ServerPlayerEntity): GuiElementBuilder {
        return GuiElementBuilder.from(
            ItemBuilder(apricorn.item().asItem())
                .setCustomName(Rib.Rib.parseText(name))
                .addLore(getButtonLore()
            ).build()
        ).setCallback { _, _, _ ->
            getButtonCallback().invoke(player)
        }
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
        return Text.literal("(${progress}/${amount})")
    }

}