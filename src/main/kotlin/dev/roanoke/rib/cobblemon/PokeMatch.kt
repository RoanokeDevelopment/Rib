package dev.roanoke.rib.cobblemon

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.item.PokemonItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

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

    fun getPokemonItem(): ItemStack {
        var properties: String = "random"
        if (species != "any") {
            properties = species.split(":")[1]
            if (form != "any") {
                properties += " $form"
            }
        }

        Rib.LOGGER.info("Being parsed: [$properties]")
        return PokemonItem.from(PokemonProperties.parse(properties, " ", "=").create())
    }

    fun matches(pokemon: Pokemon): Boolean {

        if (species != "any") {
            if (pokemon.species.resourceIdentifier.toString() != species) return false
        }

        if (form != "any") {
            if (pokemon.form.name.lowercase() != form) return false
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
