package dev.roanoke.rib.location

import dev.roanoke.rib.Rib
import kotlinx.serialization.json.*
import net.minecraft.entity.Entity
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import java.util.*

class SLocation(
    val world: ServerWorld,
    x: Double,
    y: Double,
    z: Double,
    yaw: Float? = null,
    pitch: Float? = null
): Location(
    worldId = world.registryKey.value.toString(),
    x = x,
    y = y,
    z = z,
    yaw = yaw,
    pitch = pitch
) {

    companion object : LocationFactory {

        override fun type(): String {
            return "SLocation"
        }

        override fun fromEntity(entity: Entity): SLocation {
            return SLocation(entity.world as ServerWorld, entity.x, entity.y, entity.z)
        }

        override fun fromJson(json: JsonObject): Location? {
            if (Rib.server == null)  {
                Rib.LOGGER.error("Tried to load Location class before initialising server variable.")
                return null
            }

            val x = json["x"]?.jsonPrimitive?.doubleOrNull ?: return null
            val y = json["y"]?.jsonPrimitive?.doubleOrNull ?: return null
            val z = json["z"]?.jsonPrimitive?.doubleOrNull ?: return null

            val worldId = json["world"]?.jsonPrimitive?.contentOrNull ?: return null

            val world: ServerWorld = Rib.server?.getWorld(
                RegistryKey.of(
                    RegistryKeys.WORLD,
                    Identifier.tryParse(worldId)
                )) ?: return null

            val yaw = json["yaw"]?.jsonPrimitive?.floatOrNull
            val pitch = json["pitch"]?.jsonPrimitive?.floatOrNull

            return SLocation(world, x, y, z, yaw, pitch)
        }
    }

    override fun teleportPlayer(player: ServerPlayerEntity) {

        if (yaw != null && pitch != null) {
            player.teleport(world, x, y, z, yaw, pitch)
        } else {
            player.teleport(world, x, y, z, player.yaw, player.pitch)
        }

    }

    override fun teleportPlayer(uuid: UUID) {
        Rib.server?.playerManager?.getPlayer(uuid)?.let {
            teleportPlayer(it)
        }
    }

    override fun toJson(): JsonObject {
        val data = mutableMapOf(
            "x" to JsonPrimitive(x),
            "y" to JsonPrimitive(y),
            "z" to JsonPrimitive(z),
            "world" to JsonPrimitive(world.registryKey.value.toString())
        )
        if (yaw != null && pitch != null) {
            data["yaw"] = JsonPrimitive(yaw)
            data["pitch"] = JsonPrimitive(pitch)
        }
        return JsonObject(data)
    }

}