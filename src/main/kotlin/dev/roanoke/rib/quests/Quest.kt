package dev.roanoke.rib.quests

import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import dev.roanoke.rib.cereal.JsonConv
import dev.roanoke.rib.gui.ButtonElement
import dev.roanoke.rib.quests.types.cobblemon.*
import dev.roanoke.rib.quests.types.minecraft.BreakBlockQuest
import dev.roanoke.rib.quests.types.minecraft.CraftItemQuest
import dev.roanoke.rib.quests.types.other.HasPermissionQuest
import dev.roanoke.rib.quests.types.placeholders.IntPlaceholderQuest
import dev.roanoke.rib.rewards.RewardList
import dev.roanoke.rib.utils.ItemBuilder
import dev.roanoke.rib.utils.LoreLike
import eu.pb4.sgui.api.elements.GuiElementBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
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

    fun loadDefaultValues(definition: JsonObject, state: JsonObject) {
        name = definition.get("name").asString ?: "Default Quest Title"
        id = definition.get("id")?.asString ?: UUID.randomUUID().toString()
        rewards = RewardList.fromJson(definition.get("rewards"))
        rewardsClaimed = state.get("rewardsClaimed")?.asBoolean ?: false
    }

    fun getButtonCallback(): (ServerPlayerEntity) -> Unit {
        return { player ->
            if (rewards.rewards.isNotEmpty()) {
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
        if (completed()) {
            notifyCompletion()
        }
    }

    protected fun notifyCompletion() {
        provider.onQuestComplete(this)
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

    abstract fun applyState(state: JsonObject)

    abstract fun saveSpecifics(): MutableMap<String, JsonElement>

    fun saveDefaults(): MutableMap<String, JsonElement> {
        val defaults = mutableMapOf<String, JsonElement>()
        defaults["name"] = JsonPrimitive(name)
        defaults["id"] = JsonPrimitive(id)
        defaults["rewards"] = rewards.toJson()
        defaults["rewardsClaimed"] = JsonPrimitive(rewardsClaimed)
        return defaults
    }

    fun save(): kotlinx.serialization.json.JsonObject {
        val defaults = saveDefaults()
        val specifics = saveSpecifics()
        defaults.putAll(specifics)
        return kotlinx.serialization.json.JsonObject(specifics)
    }

}