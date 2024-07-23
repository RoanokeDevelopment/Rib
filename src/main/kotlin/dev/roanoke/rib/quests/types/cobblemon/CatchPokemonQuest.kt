package dev.roanoke.rib.quests.types.cobblemon

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import dev.roanoke.rib.cobblemon.PokeMatch
import net.minecraft.text.Text
import dev.roanoke.rib.quests.Quest
import dev.roanoke.rib.quests.QuestFactory
import dev.roanoke.rib.quests.QuestGroup
import dev.roanoke.rib.quests.QuestProvider
import dev.roanoke.rib.rewards.RewardList
import dev.roanoke.rib.utils.ItemBuilder
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

class CatchPokemonQuest(name: String = "Default Catch Pokemon Quest Title",
                        id: String = UUID.randomUUID().toString(),
                        provider: QuestProvider,
                        group: QuestGroup,
                        var pokeMatch: PokeMatch = PokeMatch(),
                        var taskMessage: String = "Catch a Pokemon!",
                        var amount: Int = 1,
                        var progress: Int = 0
) :
    Quest(name, id, provider, group) {

    companion object : QuestFactory {
        override fun fromJson(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {

            Rib.LOGGER.info("Loading CatchPokemonQuest")

            val name = json.get("name").asString ?: "Default Catch Pokemon Quest Title"

            val id = json.get("id")?.asString ?: UUID.randomUUID().toString()

            var pokeMatch = PokeMatch()
            if (json.has("pokeMatch")) {
                pokeMatch = PokeMatch.fromJson(json.get("pokeMatch").asJsonObject)
            }

            var taskMessage = "Catch a pokemon!"
            if (json.has("taskMessage")) {
                taskMessage = json.get("taskMessage").asString
            }

            var amount = 3
            if (json.has("amount")) {
                amount = json.get("amount").asInt
            }

            val rRewards = RewardList.fromJson(json.get("rewards"))

            val rRewardsClaimed = state.get("rewardsClaimed")?.asBoolean ?: false

            var progress = 0
            if (json.has("progress")) {
                progress = state.get("progress")?.asInt ?: 0
            }

            return CatchPokemonQuest(name, id, provider, group, pokeMatch, taskMessage, amount, progress).apply {
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

    init {
        CobblemonEvents.POKEMON_CAPTURED.subscribe {
            Rib.LOGGER.info("Pokemon Captured")
            if (!isActive()) {
                return@subscribe
            }

            Rib.LOGGER.info("Checking if player is in group")
            if (!group.includesPlayer(it.player)) {
                Rib.LOGGER.info("Player isn't in Group")
                return@subscribe
            }

            Rib.LOGGER.info("Checking PokeMatch")
            if (!pokeMatch.matches(it.pokemon)) {
                Rib.LOGGER.info("Didn't match PokeMatch")
                return@subscribe
            }

            progress += 1
            this.notifyProgress()
/*
            if (group.includesPlayer(it.player) && pokeMatch.matches(it.pokemon)) {
                Rib.LOGGER.info("Incremementing Progress")

                progress += 1
                this.notifyProgress()
            }

 */
        }
    }

    override fun getButton(player: ServerPlayerEntity): GuiElementBuilder {
        return GuiElementBuilder.from(
            ItemBuilder(pokeMatch.getPokemonItem())
                .setCustomName(Rib.Rib.parseText(name))
                .addLore(getButtonLore()
            ).build()
        ).setCallback { _, _, _ ->
            getButtonCallback().invoke(player)
        }
    }

    override fun completed(): Boolean {
        return progress >= amount
    }

    override fun taskMessage(): Text {
        return Text.literal(taskMessage)
    }

    override fun progressMessage(): Text {
        return Text.literal("(${progress}/${amount})")
    }

}