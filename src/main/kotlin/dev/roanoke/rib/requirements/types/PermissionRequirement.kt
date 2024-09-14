package dev.roanoke.rib.requirements.types

import dev.roanoke.rib.Rib
import dev.roanoke.rib.requirements.Requirement
import dev.roanoke.rib.requirements.RequirementFactory
import dev.roanoke.rib.requirements.RequirementRegistry
import dev.roanoke.rib.utils.LoreLike
import kotlinx.serialization.json.*
import net.minecraft.server.network.ServerPlayerEntity

class PermissionRequirement(
    val permission: String,
    val prompt: String
): Requirement("PermissionRequirement") {

    companion object : RequirementFactory {

        override fun fromKson(json: JsonObject): Requirement {
            return PermissionRequirement(
                permission = json["permission"]?.jsonPrimitive?.contentOrNull ?: "",
                prompt = json["prompt"]?.jsonPrimitive?.contentOrNull ?: ""
            )
        }

    }

    override fun passesRequirement(player: ServerPlayerEntity): Boolean {
        return Rib.perm.hasPermission(player, permission)
    }

    override fun error(): LoreLike {
        return LoreLike.ofString("<red>${prompt}")
    }

    override fun prompt(): LoreLike {
        return LoreLike.ofString(prompt)
    }

    override fun saveSpecifics(): MutableMap<String, JsonElement> {
        return mutableMapOf(
            "permission" to JsonPrimitive(permission),
            "prompt" to JsonPrimitive(prompt)
        )
    }

}