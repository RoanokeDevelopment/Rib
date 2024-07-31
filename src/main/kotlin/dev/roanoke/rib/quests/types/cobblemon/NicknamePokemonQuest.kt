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
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.*

class NicknamePokemonQuest(name: String = "Nickname Pokemon Quest",
                           id: String = UUID.randomUUID().toString(),
                           type: String = "NicknamePokemonQuest",
                           provider: QuestProvider,
                           group: QuestGroup,
                           var regex: Regex = Regex(".*"),
                           var pokeMatch: PokeMatch = PokeMatch(),
                           var taskMessage: String = "Nickname a Pokemon!",
                           var amount: Int = 1,
                           var progress: Int = 0
) :
    Quest(name, id, type, provider, group) {

    companion object : QuestFactory {
        override fun fromJson(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {

            var taskMessage = "Nickname a Pokemon!"
            if (json.has("taskMessage")) {
                taskMessage = json.get("taskMessage").asString
            }

            var pokeMatch = PokeMatch()
            if (json.has("pokeMatch")) {
                pokeMatch = PokeMatch.fromJson(json.get("pokeMatch").asJsonObject)
            }

            var regex = Regex(".*")
            if (json.has("regex")) {
                regex = Regex(json.get("regex").asString)
            }

            var amount = 1
            if (json.has("amount")) {
                amount = json.get("amount").asInt
            }

            var progress = 0
            if (json.has("progress")) {
                progress = state.get("progress")?.asInt ?: 0
            }

            return NicknamePokemonQuest(
                provider = provider, group = group,
                regex = regex, pokeMatch = pokeMatch,
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

    override fun saveSpecifics(): MutableMap<String, JsonElement> {
        val specifics: MutableMap<String, JsonElement> = mutableMapOf()
        specifics["progress"] = JsonPrimitive(progress)
        specifics["amount"] = JsonPrimitive(amount)
        specifics["taskMessage"] = JsonPrimitive(taskMessage)
        specifics["pokeMatch"] = pokeMatch.toJson()
        specifics["regex"] = JsonPrimitive(regex.pattern)
        return specifics
    }

    init {
        CobblemonEvents.POKEMON_NICKNAMED.subscribe {
            if (!isActive()) {
                return@subscribe
            }

            if (group.includesPlayer(it.player)
                && regex.matches(it.nicknameString ?: "")
                && pokeMatch.matches(it.pokemon)) {

                this.progress += 1
                this.notifyProgress()

            }

        }
    }

    override fun getButton(player: ServerPlayerEntity): GuiElementBuilder {
        return GuiElementBuilder.from(
            ItemBuilder(Items.NAME_TAG)
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