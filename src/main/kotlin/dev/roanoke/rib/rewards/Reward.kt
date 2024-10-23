package dev.roanoke.rib.rewards

import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import dev.roanoke.rib.cereal.JsonConv
import eu.pb4.sgui.api.elements.GuiElementBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class Reward (
    var type: String = "command",
    var value: String = "give {player} minecraft:bread 64",
    var display: String = "- 64 Bread!"
) {

    constructor(json: JsonObject) : this() {
        json.get("type")?.let { type = it.asString }
        json.get("value")?.let { value = it.asString }
        json.get("display")?.let { display = it.asString }
    }

    constructor(json: kotlinx.serialization.json.JsonObject) : this() {
        Reward(JsonConv.kotlinJsonToGsonJson(json))
    }

    fun toJson(): kotlinx.serialization.json.JsonObject {
        val rewardMap: MutableMap<String, JsonElement> = mutableMapOf()
        rewardMap["type"] = JsonPrimitive(type)
        rewardMap["value"] = JsonPrimitive(value)
        rewardMap["display"] = JsonPrimitive(display)
        return kotlinx.serialization.json.JsonObject(rewardMap)
    }

    fun executeReward(player: ServerPlayerEntity) {
        if (type == "command") {
            val commandString = value.replace("{player}", player.gameProfile.name)
            player.server.commandManager.dispatcher.execute(commandString, player.server.commandSource)
        }
    }

    fun executeReward(placeholders: Map<String, String>) {
        if (type == "command") {
            var commandString = value
            placeholders.forEach { key, value ->
                commandString = commandString.replace(key, value)
            }
            Rib.server?.commandManager?.dispatcher?.execute(commandString, Rib.server?.commandSource)
        }
    }

    fun getGuiElement(post: List<Text> = listOf()): GuiElementBuilder {
        val lore = mutableListOf(
                "Type: <dark_aqua>${type}",
                "Value: <dark_aqua>${value}",
                "Display: <dark_aqua>${display}"
            ).map { Rib.Rib.parseText(it) }.toMutableList()
        lore.addAll(post)

        return GuiElementBuilder(Items.DIAMOND)
            .setLore(lore)
    }

}