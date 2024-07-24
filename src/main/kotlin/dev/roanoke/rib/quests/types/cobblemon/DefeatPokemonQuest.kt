package dev.roanoke.rib.quests.types.cobblemon

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import dev.roanoke.rib.cobblemon.PokeMatch
import dev.roanoke.rib.quests.Quest
import dev.roanoke.rib.quests.QuestFactory
import dev.roanoke.rib.quests.QuestGroup
import dev.roanoke.rib.quests.QuestProvider
import dev.roanoke.rib.rewards.RewardList
import dev.roanoke.rib.utils.ItemBuilder
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.*

class DefeatPokemonQuest(name: String = "Default Defeat Pokemon Quest Title",
                         id: String = UUID.randomUUID().toString(),
                         provider: QuestProvider,
                         group: QuestGroup,
                         var pokeMatch: PokeMatch = PokeMatch(),
                         var taskMessage: String = "Defeat a Pokemon in Battle!",
                         var amount: Int = 1,
                         var progress: Int = 0
) :
    Quest(name, id, provider, group) {

    companion object : QuestFactory {
        override fun fromJson(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {

            var pokeMatch = PokeMatch()
            if (json.has("pokeMatch")) {
                pokeMatch = PokeMatch.fromJson(json.get("pokeMatch").asJsonObject)
            }

            var taskMessage = "Defeat a Pokemon!"
            if (json.has("taskMessage")) {
                taskMessage = json.get("taskMessage").asString
            }

            var amount = 3
            if (json.has("amount")) {
                amount = json.get("amount").asInt
            }

            var progress = 0
            if (state.has("progress")) {
                progress = state.get("progress")?.asInt ?: 0
            }

            return CatchPokemonQuest(provider = provider,
                group = group, pokeMatch = pokeMatch,
                taskMessage = taskMessage, amount = amount,
                progress = progress).apply {
                    loadDefaultValues(json, state)
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
        CobblemonEvents.BATTLE_FAINTED.subscribe {

            if (!isActive() || !it.battle.isPvW) {
                return@subscribe
            }

            if (!it.battle.players.any { p -> group.includesPlayer(p) }) {
                return@subscribe
            }

            if (it.killed.originalPokemon.isWild() && pokeMatch.matches(it.killed.originalPokemon)) {
                progress += 1
                this.notifyProgress()
            }
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