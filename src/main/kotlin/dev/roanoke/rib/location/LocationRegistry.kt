package dev.roanoke.rib.location

import kotlinx.serialization.json.JsonObject
import net.minecraft.entity.Entity

object LocationRegistry {

    var locationFactory: LocationFactory = SLocation.Companion

    fun fromEntity(entity: Entity): Location {
        return locationFactory.fromEntity(entity)
    }

    fun fromJson(json: JsonObject): Location? {
        return locationFactory.fromJson(json)
    }

}