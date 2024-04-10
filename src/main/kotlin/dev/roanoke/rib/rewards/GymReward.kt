package dev.roanoke.rib.rewards

import com.google.gson.JsonObject
import net.minecraft.item.Items
import net.minecraft.network.message.SignedMessage
import net.minecraft.server.network.ServerPlayerEntity
import dev.roanoke.rib.Rib

class GymReward (
    var type: String = "none",
    var value: String = "",
    var gymName: String = ""
) {

    constructor(json: JsonObject, gymName: String) : this() {
        if (json.has("type")) {
            type = json.get("type").asString
        }
        if (json.has("value")) {
            value = json.get("value").asString
        }
        this.gymName = gymName
    }

    fun getRewardCommand(placeholders: MutableMap<String, String>): String {
        var command: String = value;
        placeholders.forEach {
            command = command.replace(it.key, it.value)
        }
        return command
    }

    // ONLY to be used for GUI examples
    private fun getRewardCommand(player: ServerPlayerEntity): String {
        var placeholders: MutableMap<String, String> = mutableMapOf(
            "{challenger}" to player.gameProfile.name,
            "{player}" to player.gameProfile.name,
            "{leader}" to player.gameProfile.name
        )
        return getRewardCommand(placeholders)
    }

    // placeholders: {challenger}, {player} = challenger, {leader} = gym leader
    fun executeReward(placeholders: MutableMap<String, String>) {
        if (type == "command") {
            Rib.server!!.commandManager.dispatcher.execute(
                getRewardCommand(placeholders),
                Rib.server!!.commandSource
            )
        }
    }

    fun getPlaceholders(): Map<String, String> {
        return mapOf(
            "{reward_value}" to this.value,
            "{reward_type}" to this.type
        )
    }
}