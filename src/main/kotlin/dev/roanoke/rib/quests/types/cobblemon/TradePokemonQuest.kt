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

class TradePokemonQuest(name: String = "Trade Pokemon Quest",
                        id: String = UUID.randomUUID().toString(),
                        provider: QuestProvider,
                        group: QuestGroup,
                        var sentPokemon: PokeMatch = PokeMatch(),
                        var recievedPokemon: PokeMatch = PokeMatch(),
                        var taskMessage: String = "Trade a Pokemon!",
                        var amount: Int = 1,
                        var progress: Int = 0
) :
    Quest(name, id, provider, group) {

    companion object : QuestFactory {
        override fun fromJson(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {

            var sentPokemon = PokeMatch()
            if (json.has("sentPokemon")) {
                sentPokemon = PokeMatch.fromJson(json.get("sentPokemon").asJsonObject)
            }

            var recievedPokemon = PokeMatch()
            if (json.has("recievedPokemon")) {
                recievedPokemon = PokeMatch.fromJson(json.get("recievedPokemon").asJsonObject)
            }

            var taskMessage = "Trade a Pokemon!"
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

            return TradePokemonQuest(
                provider = provider, group = group,
                sentPokemon = sentPokemon, recievedPokemon = recievedPokemon,
                taskMessage = taskMessage, amount = amount, progress = progress)
                .apply {
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

    override fun saveSpecifics(): MutableMap<String, JsonElement> {
        val specifics: MutableMap<String, JsonElement> = mutableMapOf()
        specifics["progress"] = JsonPrimitive(progress)
        specifics["amount"] = JsonPrimitive(amount)
        specifics["taskMessage"] = JsonPrimitive(taskMessage)
        specifics["recievedPokemon"] = recievedPokemon.toJson()
        specifics["sentPokemon"] = sentPokemon.toJson()
        return specifics
    }

    init {
        CobblemonEvents.TRADE_COMPLETED.subscribe {
            if (!isActive()) {
                return@subscribe
            }

            if (group.includesPlayer(it.tradeParticipant1.uuid)) {
                if (sentPokemon.matches(it.tradeParticipant1Pokemon)
                    && recievedPokemon.matches(it.tradeParticipant2Pokemon)) {
                    progress += 1
                    this.notifyProgress()
                }
            }

            if (group.includesPlayer(it.tradeParticipant2.uuid)) {
                if (sentPokemon.matches(it.tradeParticipant2Pokemon)
                    && recievedPokemon.matches(it.tradeParticipant1Pokemon)) {
                    progress += 1
                    this.notifyProgress()
                }
            }

        }
    }

    override fun getButton(player: ServerPlayerEntity): GuiElementBuilder {
        return GuiElementBuilder.from(
            ItemBuilder(sentPokemon.getPokemonItem())
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