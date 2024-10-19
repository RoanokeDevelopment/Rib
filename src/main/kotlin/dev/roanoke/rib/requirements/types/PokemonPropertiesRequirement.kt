package dev.roanoke.rib.requirements.types

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.util.party
import dev.roanoke.rib.gui.settings.SettingsManager
import dev.roanoke.rib.requirements.Requirement
import dev.roanoke.rib.requirements.RequirementFactory
import dev.roanoke.rib.utils.LoreLike
import kotlinx.serialization.json.*
import net.minecraft.server.network.ServerPlayerEntity

enum class ListType {
    BLACKLIST,
    WHITELIST
}

class PokemonPropertiesRequirement(
    val list: List<String>,
    val listType: ListType
): Requirement(
    type = "PokemonPropertiesRequirement"
) {

    val properties = list.map { PokemonProperties.parse(it) }

    companion object : RequirementFactory {

        override fun fromKson(json: JsonObject): Requirement {
            return PokemonPropertiesRequirement(
                list = json["list"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: listOf<String>(),
                listType = when((json["listType"]?.jsonPrimitive?.contentOrNull ?: "").uppercase()) {
                    "BLACKLIST" -> ListType.BLACKLIST
                    else -> ListType.WHITELIST
                }
            )
        }

    }

    init {
        registerSettings()
    }

    override fun registerSettings() {
        settings = SettingsManager(this)
    }

    override fun passesRequirement(player: ServerPlayerEntity): Boolean {
        val party = player.party()
        when(listType) {
            ListType.BLACKLIST -> {
                party.forEach { pokemon ->
                    if (properties.any { it.matches(pokemon) }) return false
                }
                return true
            }
            ListType.WHITELIST -> {
                party.forEach { pokemon ->
                    if (properties.none { it.matches(pokemon) }) return false
                }
                return true
            }
        }
    }

    override fun error(): LoreLike {
        TODO("Not yet implemented")
    }

    override fun prompt(): LoreLike {
        TODO("Not yet implemented")
    }

    override fun saveSpecifics(): MutableMap<String, JsonElement> {
        return mutableMapOf(
            "list" to JsonArray(list.map { JsonPrimitive(it) }),
            "listType" to JsonPrimitive(listType.name)
        )
    }

    override fun save() {
    }

}