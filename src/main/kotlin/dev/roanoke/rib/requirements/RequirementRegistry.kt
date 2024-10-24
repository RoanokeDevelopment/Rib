package dev.roanoke.rib.requirements

import dev.roanoke.rib.Rib
import dev.roanoke.rib.requirements.types.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

object RequirementRegistry {

    private val requirementFactories: MutableMap<String, RequirementFactory> = mutableMapOf()

    fun register(type: String, factory: RequirementFactory) {
        if (requirementFactories.containsKey(type)) {
            throw IllegalArgumentException("Requirement type $type is already registered.")
        }
        Rib.LOGGER.info("[Rib] Registered Requirement: $type")
        requirementFactories[type] = factory
    }

    fun getTypes(): List<String> {
        return requirementFactories.keys.toList()
    }

    fun getRequirements(): List<Requirement> {
        return requirementFactories.values.map {
            it.fromKson(JsonObject(mapOf()))
        }
    }

    fun getRequirement(json: JsonObject): Requirement {
        val type = json["type"]?.jsonPrimitive?.contentOrNull ?: throw IllegalArgumentException("Requirement has no type field!")

        val factory = requirementFactories[type] ?: throw IllegalArgumentException("Unsupported Requirement Type: $type")
        return factory.fromKson(json)
    }

    fun registerDefaults() {
        register("TeamLevelRequirement", TeamLevelRequirement.Companion)
        register("PokemonPropertiesRequirement", PokemonPropertiesRequirement.Companion)
        register("PermissionRequirement", PermissionRequirement.Companion)
        register("MultiRequirement", MultiRequirement.Companion)
        register("HasBattleablePokemonRequirement", HasBattleablePokemonRequirement.Companion)
    }

}