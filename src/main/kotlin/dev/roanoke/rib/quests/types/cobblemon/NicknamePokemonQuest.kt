package dev.roanoke.rib.quests.types.cobblemon

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import dev.roanoke.rib.cobblemon.PokeMatch
import dev.roanoke.rib.quests.Quest
import dev.roanoke.rib.quests.QuestGroup
import dev.roanoke.rib.quests.QuestProvider
import dev.roanoke.rib.rewards.RewardList
import dev.roanoke.rib.utils.ItemBuilder
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.*

class NicknamePokemonQuest(name: String = "Nickname Pokemon Quest",
                           id: String = UUID.randomUUID().toString(),
                           provider: QuestProvider,
                           group: QuestGroup,
                           var regex: Regex = Regex(".*"),
                           var pokeMatch: PokeMatch = PokeMatch(),
                           var taskMessage: String = "Nickname a Pokemon!",
                           var amount: Int = 1,
                           var progress: Int = 0
) :
    Quest(name, id, provider, group) {

    companion object : Quest.QuestFactory {
        override fun fromState(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {
            val name = json.get("name").asString ?: "Nickname Pokemon Quest"

            val id = json.get("id")?.asString ?: UUID.randomUUID().toString()

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

            val rRewards = RewardList.fromJson(json.get("rewards"))

            val rRewardsClaimed = state.get("rewardsClaimed")?.asBoolean ?: false

            var progress = 0
            if (json.has("progress")) {
                progress = state.get("progress")?.asInt ?: 0
            }

            return NicknamePokemonQuest(
                name, id, provider, group,
                regex = regex, pokeMatch = pokeMatch,
                taskMessage, amount, progress)
                .apply {
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