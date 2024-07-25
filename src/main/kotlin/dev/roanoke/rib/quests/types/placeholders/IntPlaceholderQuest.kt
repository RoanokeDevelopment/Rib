package dev.roanoke.rib.quests.types.placeholders

import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import dev.roanoke.rib.quests.Quest
import dev.roanoke.rib.quests.QuestFactory
import dev.roanoke.rib.quests.QuestGroup
import dev.roanoke.rib.quests.QuestProvider
import dev.roanoke.rib.utils.ItemBuilder
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.sgui.api.elements.GuiElementBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.*
import kotlin.math.max

class IntPlaceholderQuest(name: String = "Default IntPlaceholder Quest Title",
                          id: String = UUID.randomUUID().toString(),
                          provider: QuestProvider,
                          group: QuestGroup,
                          var item: ItemBuilder,
                          var taskMessage: String,
                          var placeholder: String,
                          var amount: Int = 6
) :
    Quest(name, id, provider, group) {

    companion object : QuestFactory {
        override fun fromJson(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {

            val placeholder = json.get("placeholder").asString

            val amount = json.get("amount").asInt
            val item = ItemBuilder.fromJson(json.get("item").asJsonObject)
            val taskMessage = json.get("taskMessage").asString

            return IntPlaceholderQuest(
                provider = provider, group = group,
                item = item, taskMessage = taskMessage,
                placeholder = placeholder, amount = amount).apply {
                    loadDefaultValues(json, state)
            }
        }
    }

    override fun getState(): JsonObject {
        return JsonObject().apply {
            addProperty("rewardsClaimed", rewardsClaimed)
        }
    }

    override fun applyState(state: JsonObject) {
        rewardsClaimed = state.get("rewardsClaimed")?.asBoolean ?: rewardsClaimed
    }

    override fun saveSpecifics(): MutableMap<String, JsonElement> {
        val specifics: MutableMap<String, JsonElement> = mutableMapOf()
        specifics["placeholder"] = JsonPrimitive(placeholder)
        specifics["item"] = JsonPrimitive(Registries.ITEM.getId(item.build().item).toString())
        specifics["taskMessage"] = JsonPrimitive(taskMessage)
        specifics["amount"] = JsonPrimitive(amount)
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

    fun getProgress(): Int {

        var progress = 0
        group.getOnlinePlayers().forEach {
            val placeholderResult = Placeholders.parseText(Text.literal(placeholder), PlaceholderContext.of(it))

            val inputString = placeholderResult.content.toString()
            val regex = "\\{(.*?)}".toRegex()
            val matchResult = regex.find(inputString)

            val valueInsideBraces = matchResult?.groups?.get(1)?.value

            Rib.LOGGER.info("Got Placeholder Result: ${placeholderResult.content.toString()}")
            progress = max(progress, valueInsideBraces?.toInt() ?: 0)
            Rib.LOGGER.info("Parsed, new progress is: $progress")
        }

        return progress

    }

    override fun completed(): Boolean {
        return getProgress() >= amount
    }

    override fun taskMessage(): Text {
        return Rib.Rib.parseText(taskMessage)
    }

    override fun progressMessage(): Text {
        return Text.literal("(${getProgress()}/${amount})")
    }

}