package dev.roanoke.rib.quests.types.cobblemon

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import dev.roanoke.rib.cobblemon.PokeMatch
import dev.roanoke.rib.quests.Quest
import dev.roanoke.rib.quests.QuestFactory
import dev.roanoke.rib.quests.QuestGroup
import dev.roanoke.rib.quests.QuestProvider
import dev.roanoke.rib.utils.ItemBuilder
import eu.pb4.sgui.api.elements.GuiElementBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.*

class DefeatPokemonQuest(name: String = "Default Defeat Pokemon Quest Title",
                         id: String = UUID.randomUUID().toString(),
                         type: String = "DefeatPokemonQuest",
                         provider: QuestProvider,
                         group: QuestGroup,
                         var pokeMatch: PokeMatch = PokeMatch(),
                         var taskMessage: String = "Defeat a Pokemon in Battle!",
                         var amount: Int = 1,
                         var progress: Int = 0
) :
    Quest(name, id, type, provider, group) {

    companion object : QuestFactory {
        override fun fromJson(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {

            var pokeMatch = PokeMatch()
            json["pokeMatch"]?.let {
                pokeMatch = PokeMatch.fromJson(it.asJsonObject)
            }

            val taskMessage = json["taskMessage"]?.asString ?: "Defeat a Pokemon"

            val amount = json["amount"]?.asInt ?: 1

            val progress = state["progress"]?.asInt ?: 0

            return DefeatPokemonQuest(provider = provider,
                group = group, pokeMatch = pokeMatch,
                taskMessage = taskMessage, amount = amount,
                progress = progress).apply {
                    loadDefaultValues(json, state)
            }
        }
    }

    override fun getQuestState(): JsonObject {
        return JsonObject().apply {
            addProperty("progress", progress)
        }
    }

    override fun applyQuestState(state: JsonObject) {
        progress = state.get("progress")?.asInt ?: progress
    }

    override fun saveSpecifics(): MutableMap<String, JsonElement> {
        val specifics: MutableMap<String, JsonElement> = mutableMapOf()
        specifics["progress"] = JsonPrimitive(progress)
        specifics["amount"] = JsonPrimitive(amount)
        specifics["taskMessage"] = JsonPrimitive(taskMessage)
        specifics["pokeMatch"] = pokeMatch.toJson()
        return specifics
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