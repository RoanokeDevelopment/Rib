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
                     uuid: UUID = UUID.randomUUID(),
                     provider: QuestProvider,
                     group: QuestGroup,
                     var item: Item,
                     var amount: Int = 3,
                     var progress: Int = 0
) :
    Quest(name, uuid, provider, group) {

    companion object : Quest.QuestFactory {
        override fun fromState(json: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {
            val name = json.get("name").asString
            val uuid = UUID.fromString(json.get("uuid").asString)

            val itemID = json.get("item").asString
            val item: Item = Registries.ITEM.get(Identifier.tryParse(itemID))

            val amount = json.get("amount").asInt
            val progress = json.get("progress").asInt

            return CraftItemQuest(name, uuid, provider, group, item, amount, progress)
        }
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
        jsonObject.addProperty("uuid", id.toString())
        jsonObject.addProperty("item", Registries.ITEM.getId(item).toString())
        jsonObject.addProperty("amount", amount)
        jsonObject.addProperty("progress", progress)
        return jsonObject
    }
}