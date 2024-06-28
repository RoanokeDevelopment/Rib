package dev.roanoke.rib.rewards

import com.google.gson.JsonObject
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

    fun executeReward(player: ServerPlayerEntity) {
        if (type == "command") {
            val commandString = value.replace("{player}", player.gameProfile.name)
            player.server.commandManager.dispatcher.execute(commandString, player.server.commandSource)
        }
    }

}