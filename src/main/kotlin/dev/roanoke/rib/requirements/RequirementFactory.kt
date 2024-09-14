package dev.roanoke.rib.requirements

import kotlinx.serialization.json.JsonObject

interface RequirementFactory {

    fun fromKson(json: JsonObject): Requirement

}