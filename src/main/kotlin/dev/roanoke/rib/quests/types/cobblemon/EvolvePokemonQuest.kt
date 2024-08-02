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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.*

class EvolvePokemonQuest(name: String = "Evolve Pokemon Quest",
                         id: String = UUID.randomUUID().toString(),
                         type: String = "EvolvePokemonQuest",
                         provider: QuestProvider,
                         group: QuestGroup,
                         var preEvolution: PokeMatch = PokeMatch(),
                         var postEvolution: PokeMatch = PokeMatch(),
                         var taskMessage: String = "Evolve a Pokemon!",
                         var amount: Int = 1,
                         var progress: Int = 0
) :
    Quest(name, id, type, provider, group) {

    companion object : QuestFactory {
        override fun fromJson(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {

            var preEvolution = PokeMatch()
            if (json.has("preEvolution")) {
                preEvolution = PokeMatch.fromJson(json.get("preEvolution").asJsonObject)
            }

            var postEvolution = PokeMatch()
            if (json.has("postEvolution")) {
                postEvolution = PokeMatch.fromJson(json.get("postEvolution").asJsonObject)
            }

            var taskMessage = "Evolve a Pokemon!"
            if (json.has("taskMessage")) {
                taskMessage = json.get("taskMessage").asString
            }

            var amount = 1
            if (json.has("amount")) {
                amount = json.get("amount").asInt
            }

            var progress = 0
            if (json.has("progress")) {
                progress = state.get("progress")?.asInt ?: 0
            }

            return EvolvePokemonQuest(
                provider = provider, group = group,
                preEvolution = preEvolution, postEvolution = postEvolution,
                taskMessage = taskMessage, amount = amount, progress = progress)
                .apply {
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
        specifics["preEvolution"] = preEvolution.toJson()
        specifics["postEvolution"] = postEvolution.toJson()
        return specifics
    }

    init {
        CobblemonEvents.EVOLUTION_COMPLETE.subscribe {
            if (!isActive()) {
                return@subscribe
            }

            it.pokemon.getOwnerPlayer()?.let { player ->
                if (group.includesPlayer(player)) {
                    if (preEvolution.matches(it.pokemon)
                        && postEvolution.matches(it.evolution.result.create())) {

                        progress += 1
                        this.notifyProgress()

                    }
                }
            }

        }
    }

    override fun getButton(player: ServerPlayerEntity): GuiElementBuilder {
        return GuiElementBuilder.from(
            ItemBuilder(postEvolution.getPokemonItem())
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