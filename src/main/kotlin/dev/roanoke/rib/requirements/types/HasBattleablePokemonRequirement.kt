package dev.roanoke.rib.requirements.types

import com.cobblemon.mod.common.util.party
import dev.roanoke.rib.requirements.Requirement
import dev.roanoke.rib.requirements.RequirementFactory
import dev.roanoke.rib.requirements.RequirementRegistry
import dev.roanoke.rib.utils.LoreLike
import kotlinx.serialization.json.*
import net.minecraft.server.network.ServerPlayerEntity

class HasBattleablePokemonRequirement: Requirement("HasBattleablePokemonRequirement") {

    companion object : RequirementFactory {

        override fun fromKson(json: JsonObject): Requirement {
            return HasBattleablePokemonRequirement()
        }

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

}