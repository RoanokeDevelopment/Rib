package dev.roanoke.rib.quests.types.minecraft

import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import dev.roanoke.rib.callbacks.ItemCraftedCallback
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import dev.roanoke.rib.quests.Quest
import dev.roanoke.rib.quests.QuestFactory
import dev.roanoke.rib.quests.QuestGroup
import dev.roanoke.rib.quests.QuestProvider
import dev.roanoke.rib.rewards.RewardList
import dev.roanoke.rib.utils.ItemBuilder
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

class CraftItemQuest(name: String = "Default Craft Quest Title",
                     id: String = UUID.randomUUID().toString(),
                     provider: QuestProvider,
                     group: QuestGroup,
                     var item: Item,
                     var amount: Int = 3,
                     var progress: Int = 0
) :
    Quest(name, id, provider, group) {

    companion object : QuestFactory {
        override fun fromJson(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {

            val itemID = json.get("item").asString
            val item: Item = Registries.ITEM.get(Identifier.tryParse(itemID))

            val amount = json.get("amount").asInt

            val progress = state.get("progress")?.asInt ?: 0

            return CraftItemQuest(
                provider = provider, group = group,
                item = item, amount = amount,
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

    override fun getButton(player: ServerPlayerEntity): GuiElementBuilder {
        return GuiElementBuilder.from(
            ItemBuilder(item)
                .setCustomName(Rib.Rib.parseText(name))
                .addLore(getButtonLore()
            ).build()
        ).setCallback { _, _, _ ->
            getButtonCallback().invoke(player)
        }
    }

    init {
        ItemCraftedCallback.EVENT.register { player, itemStack, amount ->
            if (!isActive()) { return@register }

            if (group.includesPlayer(player) && !completed()) {
                if (itemStack.isOf(this.item)) {
                    this.progress += amount
                    this.notifyProgress()
                }
            }
        }
    }

    override fun completed(): Boolean {
        return progress >= amount
    }

    override fun taskMessage(): Text {
        return Text.literal("Craft ${amount}x ").append(item.name)
    }

    override fun progressMessage(): Text {
        return Text.literal("(${progress}/${amount})")
    }

}