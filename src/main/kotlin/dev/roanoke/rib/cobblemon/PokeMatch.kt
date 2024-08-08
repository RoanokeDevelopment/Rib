package dev.roanoke.rib.cobblemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.item.PokemonItem
import com.cobblemon.mod.common.pokemon.Pokemon
import dev.roanoke.rib.Rib
import dev.roanoke.rib.cereal.JsonConv
import kotlinx.serialization.json.*
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class PokeMatch(
    val species: String = "",
    val form: String = "",
    val types: List<String> = listOf(),
    val aspects: List<String> = listOf(),
    val ivs: Map<String, Int>? = mapOf(),
    val evs: Map<String, Int>? = mapOf(),
    val shiny: Boolean? = null
) {

    companion object {
        fun fromJson(json: JsonObject): PokeMatch {

            val species = json["species"]?.jsonPrimitive?.contentOrNull ?: ""
            val form = json["form"]?.jsonPrimitive?.contentOrNull ?: ""
            val types = json["types"]?.jsonArray?.map {
                it.jsonPrimitive.content
            } ?: listOf()
            val aspects = json["aspects"]?.jsonArray?.map {
                it.jsonPrimitive.content
            } ?: listOf()
            val shiny = json["shiny"]?.jsonPrimitive?.booleanOrNull

            val ivs = json["ivs"]?.jsonObject?.map { entry ->
                entry.key to entry.value.jsonPrimitive.int
            }?.toMap()

            val evs = json["evs"]?.jsonObject?.map { entry ->
                entry.key to entry.value.jsonPrimitive.int
            }?.toMap()

            return PokeMatch(
                species = species,
                form = form,
                types = types,
                aspects = aspects,
                ivs = ivs,
                evs = evs,
                shiny = shiny
            )
        }

        fun fromJson(json: com.google.gson.JsonObject): PokeMatch {
            return fromJson(JsonConv.gsonToKotlinJson(json))
        }
    }

    fun getPokemonItem(): ItemStack {
        var properties: String = "random"
        if (species != "") {
            properties = species.split(":")[1]
            if (form != "") {
                properties += " $form"
            }
        }

        aspects.forEach {
            properties += " $it"
        }

        shiny?.let {
            properties += " shiny=${shiny.toString().lowercase()}"
        }

        return PokemonItem.from(PokemonProperties.parse(properties, " ", "=").create())
    }

    fun matches(pokemon: Pokemon): Boolean {

        if (species != "") {
            if (pokemon.species.resourceIdentifier.toString() != species) return false
        }

        if (form != "") {
            if (pokemon.form.name.lowercase() != form) return false
        }

        types.forEach { pmt ->
            if (!pokemon.types.map { type -> type.name.lowercase() }
                .contains(pmt.lowercase())) {
                return false
            }
        }

        shiny?.let {
            if (pokemon.shiny != shiny) {
                return false
            }
        }

        ivs?.let {
            if (!pokemon.ivs.matchesMap(ivs)) {
                return false
            }
        }

        evs?.let {
            if (!pokemon.evs.matchesMap(evs)) {
                return false
            }
        }

        return pokemon.aspects.containsAll(aspects)

    }

    fun toJson(): JsonObject {
        val match: MutableMap<String, JsonElement> = mutableMapOf()
        match["species"] = JsonPrimitive(species)
        match["form"] = JsonPrimitive(form)
        shiny?.let {
            match["shiny"] = JsonPrimitive(shiny)
        }
        match["aspects"] = JsonArray(aspects.map { JsonPrimitive(it) })
        match["types"] = JsonArray(types.map { JsonPrimitive(it) })

        ivs?.let {
            val ivsJson = JsonObject(it.map { (key, value) -> key to JsonPrimitive(value) }.toMap())
            match["ivs"] = ivsJson
        }
        evs?.let {
            val evsJson = JsonObject(it.map { (key, value) -> key to JsonPrimitive(value) }.toMap())
            match["evs"] = evsJson
        }

        return JsonObject(match)
    }
}
