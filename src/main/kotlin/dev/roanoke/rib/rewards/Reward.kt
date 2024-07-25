package dev.roanoke.rib.rewards

import com.google.gson.JsonObject
import dev.roanoke.rib.cereal.JsonConv
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import net.minecraft.server.network.ServerPlayerEntity

class Reward (
    var type: String = "none",
    var value: String = "",
    var display: String = ""
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

}