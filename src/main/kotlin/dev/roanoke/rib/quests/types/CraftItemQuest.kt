package dev.roanoke.rib.quests.types

import com.google.gson.JsonObject
import dev.roanoke.rib.callbacks.ItemCraftedCallback
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import dev.roanoke.rib.quests.Quest
import dev.roanoke.rib.quests.QuestGroup
import dev.roanoke.rib.quests.QuestProvider
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

    companion object : Quest.QuestFactory {
        override fun fromState(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {
            val name = json.get("name")?.asString ?: "Default Craft Quest Title"
            val id = json.get("id")?.asString ?: UUID.randomUUID().toString()

            val itemID = json.get("item").asString
            val item: Item = Registries.ITEM.get(Identifier.tryParse(itemID))

            val amount = json.get("amount").asInt

            val progress = state.get("progress")?.asInt ?: 0

            return CraftItemQuest(name, id, provider, group, item, amount, progress)
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
        ItemCraftedCallback.EVENT.register { player, itemStack, amount ->
            if (!isActive()) { return@register }

            if (group.includesPlayer(player) && !completed()) {
                if (itemStack.isOf(this.item)) {
                    this.progress += amount
                    this.notifyProgress()
                    if (completed()) {
                        this.notifyCompletion()
                    }
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
        return Text.literal("${progress} / ${amount}")
    }

    override fun saveState(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", "CraftItemQuest")
        jsonObject.addProperty("name", name)
        jsonObject.addProperty("id", id.toString())
        jsonObject.addProperty("item", Registries.ITEM.getId(item).toString())
        jsonObject.addProperty("amount", amount)
        jsonObject.addProperty("progress", progress)
        return jsonObject
    }
}