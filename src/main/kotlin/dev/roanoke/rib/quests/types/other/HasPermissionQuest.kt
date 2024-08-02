package dev.roanoke.rib.quests.types.other

import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import dev.roanoke.rib.quests.Quest
import dev.roanoke.rib.quests.QuestFactory
import dev.roanoke.rib.quests.QuestGroup
import dev.roanoke.rib.quests.QuestProvider
import dev.roanoke.rib.utils.ItemBuilder
import eu.pb4.sgui.api.elements.GuiElementBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.*

class HasPermissionQuest(name: String = "Default Has Permission Quest Title",
                         id: String = UUID.randomUUID().toString(),
                         type: String = "HasPermissionQuest",
                         provider: QuestProvider,
                         group: QuestGroup,
                         var item: ItemBuilder,
                         var taskMessage: String,
                         var permission: String
    ) :
    Quest(name, id, type, provider, group) {

    companion object : QuestFactory {
        override fun fromJson(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {

            val permission = json.get("permission").asString

            val item = ItemBuilder.fromJson(json.get("item").asJsonObject)

            val taskMessage = json.get("taskMessage").asString

            return HasPermissionQuest(
                provider = provider, group = group,
                item = item, taskMessage = taskMessage,
                permission = permission).apply {
                    loadDefaultValues(json, state)
            }
        }
    }

    override fun getQuestState(): JsonObject {
        return JsonObject()
    }

    override fun applyQuestState(state: JsonObject) { }

    override fun saveSpecifics(): MutableMap<String, JsonElement> {
        val specifics: MutableMap<String, JsonElement> = mutableMapOf()
        specifics["permission"] = JsonPrimitive(permission)
        specifics["item"] = JsonPrimitive(Registries.ITEM.getId(item.build().item).toString())
        specifics["taskMessage"] = JsonPrimitive(taskMessage)
        return specifics
    }

    override fun getButton(player: ServerPlayerEntity): GuiElementBuilder {
        return GuiElementBuilder.from(
            item
                .setCustomName(Rib.Rib.parseText(name))
                .addLore(getButtonLore()
            ).build()
        ).setCallback { _, _, _ ->
            getButtonCallback().invoke(player)
        }
    }

    override fun completed(): Boolean {
        return group.getOnlinePlayers().any {
            Rib.perm.hasPermission(it, permission)
        }
    }

    override fun taskMessage(): Text {
        return Rib.Rib.parseText(taskMessage)
    }

    override fun progressMessage(): Text {
        return Text.literal("(${if (!completed()) "0" else "1"}/1)")
    }

}