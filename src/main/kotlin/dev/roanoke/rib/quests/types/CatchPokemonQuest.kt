package dev.roanoke.rib.quests.types

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import dev.roanoke.rib.cobblemon.PokeMatch
import net.minecraft.text.Text
import dev.roanoke.rib.quests.Quest
import dev.roanoke.rib.quests.QuestGroup
import dev.roanoke.rib.quests.QuestProvider
import dev.roanoke.rib.utils.ItemBuilder
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.item.Items
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

    companion object : Quest.QuestFactory {
        override fun fromState(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {
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

            var progress = 0
            if (json.has("progress")) {
                progress = state.get("progress")?.asInt ?: 0
            }

            return CatchPokemonQuest(name, id, provider, group, pokeMatch, taskMessage, amount, progress)
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