package dev.roanoke.rib.utils

import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import net.minecraft.entity.Entity
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier

class Location(
    val world: ServerWorld,
    val x: Double,
    val y: Double,
    val z: Double
) {

    companion object {
        fun fromJson(json: JsonObject): Location? {

            if (Rib.server == null)  {
                Rib.LOGGER.error("Tried to load Location class before initialising server variable.")
                return null
            }

            var x = json.get("x").asDouble
            var y = json.get("y").asDouble
            var z = json.get("z").asDouble

            val world: ServerWorld? = Rib.server!!.getWorld(
                RegistryKey.of(
                    RegistryKeys.WORLD,
                    Identifier.tryParse(json.get("world").asString)
                ))
            if (world == null) {
                Rib.LOGGER.error("Failed to load Location as failed to get ServerWorld")
                return null
            }

            return Location(world, x, y, z)

        }

        fun fromMap(map: MutableMap<String, Any>): Location? {
            var x = map["x"] as Double
            var y = map["y"] as Double
            var z = map["z"] as Double

            var world: ServerWorld? = Rib.server!!.getWorld(
                RegistryKey.of(
                    RegistryKeys.WORLD,
                    Identifier.tryParse(map["world"].toString())
                )
            )

            if (world == null) { return null }

            return Location(world, x, y, z)
        }

        fun fromEntity(entity: Entity): Location  {
            return Location(entity.world as ServerWorld, entity.x, entity.y, entity.z)
        }
    }

    fun toJsonObject(): JsonObject {
        val jsonObj = JsonObject()
        jsonObj.addProperty("x", x)
        jsonObj.addProperty("y", y)
        jsonObj.addProperty("z", z)
        jsonObj.addProperty("world", world.registryKey.value.toString())
        return jsonObj
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "x" to x,
            "y" to y,
            "z" to z,
            "world" to world.registryKey.value.toString()
        )
    }

    fun teleportPlayer(player: ServerPlayerEntity) {
        player.teleport(world, x, y, z, player.yaw, player.pitch)
    }

}