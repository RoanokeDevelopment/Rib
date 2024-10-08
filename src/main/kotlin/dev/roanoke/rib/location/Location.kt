package dev.roanoke.rib.location

import kotlinx.serialization.json.JsonObject
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import java.util.UUID

abstract class Location(
    val worldId: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float? = null,
    val pitch: Float? = null
) {

    val vec3d: Vec3d get() = Vec3d(x, y, z)

    abstract fun teleportPlayer(player: ServerPlayerEntity)

    abstract fun teleportPlayer(uuid: UUID)

    abstract fun toJson(): JsonObject

}