package dev.roanoke.rib.location

import kotlinx.serialization.json.JsonObject
import net.minecraft.entity.Entity

interface LocationFactory {

    fun type(): String

    fun fromEntity(entity: Entity): Location

    fun fromJson(json: JsonObject): Location?

}