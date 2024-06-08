package dev.roanoke.rib.quests.types

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import dev.roanoke.rib.cobblemon.PokeMatch
import dev.roanoke.rib.quests.Quest
import dev.roanoke.rib.quests.QuestGroup
import dev.roanoke.rib.quests.QuestProvider
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

    companion object : Quest.QuestFactory {
        override fun fromState(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {
            val name = json.get("name").asString ?: "Default Defeat Pokemon Quest Title"

            val id = json.get("id")?.asString ?: UUID.randomUUID().toString()

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

            return DefeatPokemonQuest(name, id, provider, group, pokeMatch, taskMessage, amount, progress)
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
                if (completed()) {
                    this.notifyCompletion()
                }
            }
        }
    }

    override fun getButton(player: ServerPlayerEntity): GuiElementBuilder {
        return GuiElementBuilder.from(
            ItemBuilder(pokeMatch.getPokemonItem())
                .setCustomName(Rib.Rib.parseText(name))
                .addLore(listOf(
                    progressMessage()
                )
            ).build()
        )
    }

    override fun completed(): Boolean {
        return progress >= amount
    }

    override fun taskMessage(): Text {
        return Text.literal(taskMessage)
    }

    override fun progressMessage(): Text {
        return Text.literal("$taskMessage (${progress}/${amount})")
    }

    override fun saveState(): JsonObject {
        val jsonObject = JsonObject()
        return jsonObject
    }
}