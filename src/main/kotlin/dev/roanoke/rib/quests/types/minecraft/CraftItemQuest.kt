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
import dev.roanoke.rib.utils.ItemBuilder
import eu.pb4.sgui.api.elements.GuiElementBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

class CraftItemQuest(name: String = "Default Craft Quest Title",
                     id: String = UUID.randomUUID().toString(),
                     type: String = "CraftItemQuest",
                     provider: QuestProvider,
                     group: QuestGroup,
                     var item: Item,
                     var amount: Int = 3,
                     var progress: Int = 0
) :
    Quest(name, id, type, provider, group) {

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
        specifics["item"] = JsonPrimitive(Registries.ITEM.getId(item).toString())
        return specifics
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