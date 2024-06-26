package dev.roanoke.rib.quests

import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import dev.roanoke.rib.gui.ButtonElement
import dev.roanoke.rib.quests.types.*
import dev.roanoke.rib.rewards.RewardList
import dev.roanoke.rib.utils.ItemBuilder
import dev.roanoke.rib.utils.LoreLike
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.UUID

abstract class Quest(
    var name: String = "Default Quest Title",
    var id: String = UUID.randomUUID().toString(),
    var provider: QuestProvider,
    var group: QuestGroup,
    var rewards: RewardList = RewardList(),
    var rewardsClaimed: Boolean = false
): QuestLike, ButtonElement {

    override fun getButton(player: ServerPlayerEntity): GuiElementBuilder {
        return GuiElementBuilder.from(
            ItemBuilder(Items.STONE)
                .setCustomName(Rib.Rib.parseText(name))
                .addLore(getButtonLore()
            ).build()
        ).setCallback { _, _, _ ->
            getButtonCallback().invoke(player)
        }
    }

    fun getButtonCallback(): (ServerPlayerEntity) -> Unit {
        return { player ->

            if (completed()) {
                if (rewardsClaimed) {
                    player.sendMessage(Text.literal("These rewards have already been claimed!"))
                } else {
                    player.sendMessage(Text.literal("Redeemed Quest Rewards"))
                    claimRewards(player)
                }
            } else {
                player.sendMessage(Text.literal("You need to actually finish the Quest before you can have your rewards!"))
            }
        }
    }

    fun getButtonLore(): LoreLike {
        val loreList: MutableList<Text> = mutableListOf()

        loreList.add(Text.literal(""))
        loreList.add(taskAndProgress())
        loreList.add(Text.literal(""))

        if (rewards.rewards.isNotEmpty()) {
            loreList.add(Rib.Rib.parseText("<gold><bold>Rewards"))
        }
        rewards.rewards.forEach {
            loreList.add(Rib.Rib.parseText("- " + it.display))
        }

        if (completed() && rewards.rewards.isNotEmpty()) {
            loreList.add(Text.literal(""))
            if (rewardsClaimed) {
                loreList.add(Text.literal("Rewards have been claimed."))
            } else {
                loreList.add(Text.literal("Left click to claim Rewards!"))
            }
        }

        return LoreLike(loreList)
    }


    fun claimRewards(player: ServerPlayerEntity) {
        rewards.executeRewards(player)
        rewardsClaimed = true
        provider.onRewardsClaimed(this)
    }

    protected fun notifyProgress() {
        provider.onQuestProgress(this)
    }

    protected fun notifyCompletion() {
        if (completed()) {
            provider.onQuestComplete(this)
        }
    }

    override fun description(): LoreLike {
        return LoreLike.ofText(taskMessage())
    }

    override fun progress(): LoreLike {
        return LoreLike.ofText(progressMessage())
    }

    override fun isActive(): Boolean {
        return (!completed() && provider.isQuestActive(this))
    }

    abstract fun taskMessage(): Text

    abstract fun progressMessage(): Text

    override fun taskAndProgress(): Text {
        return taskMessage().copy().append(Text.literal(" ")).append(progressMessage())
    }

    abstract fun getState(): JsonObject

    @Deprecated(
        message = "The definition & state of a Quest should be stored separately. Use getState() to get relevant stateful information as a JsonObject",
        replaceWith = ReplaceWith("getState()")
    )
    abstract fun saveState(): JsonObject

    abstract fun applyState(state: JsonObject)

    interface QuestFactory {
        fun fromState(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest

    }

    companion object : QuestFactory {
        override fun fromState(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {
            throw UnsupportedOperationException("Use fromJson method instead.")
        }

        fun fromJson(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {
            val type = json.get("type").asString
            return when (type) {
                "BreakBlockQuest" -> BreakBlockQuest.fromState(json, state, provider, group)
                "CraftItemQuest" -> CraftItemQuest.fromState(json, state, provider, group)
                "CatchPokemonQuest" -> CatchPokemonQuest.fromState(json, state, provider, group)
                "HarvestApricornQuest" -> HarvestApricornQuest.fromState(json, state, provider, group)
                "DefeatPokemonQuest" -> DefeatPokemonQuest.fromState(json, state, provider, group)
                "IntPlaceholderQuest" -> IntPlaceholderQuest.fromState(json, state, provider, group)
                else -> throw IllegalArgumentException("Unsupported quest type: $type")
            }
        }
    }

}