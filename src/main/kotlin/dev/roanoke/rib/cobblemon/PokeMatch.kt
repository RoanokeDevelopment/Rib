package dev.roanoke.rib.cobblemon

import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.JsonObject

class PokeMatch(
    val species: String = "any",
    val form: String = "any"
) {

    companion object {
        fun fromJson(json: JsonObject): PokeMatch {
            var species = "any"
            if (json.has("species")) {
                species = json.get("species").asString
            }

            var form = "any"
            if (json.has("form")) {
                form = json.get("form").asString
            }

            return PokeMatch(species, form)
        }
    }

    fun matches(pokemon: Pokemon): Boolean {

        if (species != "any") {
            if (pokemon.species.resourceIdentifier.toString() != species) return false
        }

        return true

    }

    fun toJson(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("species", species)
        jsonObject.addProperty("form", form)
        return jsonObject
    }
}
