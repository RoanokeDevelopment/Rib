package dev.roanoke.rib.requirements.types

import com.cobblemon.mod.common.util.party
import dev.roanoke.rib.gui.settings.SettingsManager
import dev.roanoke.rib.requirements.Requirement
import dev.roanoke.rib.requirements.RequirementFactory
import dev.roanoke.rib.requirements.RequirementRegistry
import dev.roanoke.rib.utils.LoreLike
import kotlinx.serialization.json.*
import net.minecraft.server.network.ServerPlayerEntity

class HasBattleablePokemonRequirement: Requirement("HasBattleablePokemonRequirement", "Has Battleable Pokemon") {

    companion object : RequirementFactory {

        override fun fromKson(json: JsonObject): Requirement {
            return HasBattleablePokemonRequirement()
        }

    }

    init {
        registerSettings()
    }

    override fun registerSettings() {
        settings = SettingsManager(this)
    }

    override fun description(): List<String> {
        return listOf(
            "Requires the player to have a Pokemon that can battle in their party",
            "Basically - do they have at least one non-fainted Pokemon"
        )
    }

    override fun passesRequirement(player: ServerPlayerEntity): Boolean {
        val party = player.party()
        return party.any {
            !it.isFainted()
        }
    }

    override fun error(): LoreLike {
        return LoreLike.ofString("<red>You need a Pokemon that can battle!")
    }

    override fun prompt(): LoreLike {
        return LoreLike.ofString("You need a Pokemon that can battle!")
    }

    override fun saveSpecifics(): MutableMap<String, JsonElement> {
        return mutableMapOf()
    }

    override fun save() {
        TODO("Not yet implemented")
    }

}