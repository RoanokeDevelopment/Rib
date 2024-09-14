package dev.roanoke.rib.requirements

import dev.roanoke.rib.utils.LoreLike
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import net.minecraft.server.network.ServerPlayerEntity

abstract class Requirement(
    val type: String
) {

    abstract fun passesRequirement(player: ServerPlayerEntity): Boolean

    abstract fun error(): LoreLike

    abstract fun prompt(): LoreLike

    abstract fun saveSpecifics(): MutableMap<String, JsonElement>

    fun toKson(): JsonObject {
        return JsonObject(
            saveSpecifics().also {
                it["type"] = JsonPrimitive(type)
            }
        )
    }

}