package dev.roanoke.rib.rewards

import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import dev.roanoke.rib.cereal.JsonConv
import eu.pb4.sgui.api.elements.GuiElementBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity

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

    fun getGuiElement(): GuiElementBuilder {
        return GuiElementBuilder(Items.DIAMOND)
            .setLore(listOf(
                "Type: <dark_aqua>${type}",
                "Value: <dark_aqua>${value}",
                "Display: <dark_aqua>${display}"
            ).map { Rib.Rib.parseText(it) })
    }

}