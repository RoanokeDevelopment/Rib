package dev.roanoke.rib.requirements.types

import com.cobblemon.mod.common.util.party
import dev.roanoke.rib.requirements.Requirement
import dev.roanoke.rib.requirements.RequirementFactory
import dev.roanoke.rib.utils.LoreLike
import kotlinx.serialization.json.*
import net.minecraft.server.network.ServerPlayerEntity

enum class LevelComparison {
    MINIMUM,
    EXACT,
    MAXIMUM
}

class TeamLevelRequirement(
    val level: Int,
    val comparison: LevelComparison
): Requirement(
    type = "TeamLevelRequirement"
) {

    companion object : RequirementFactory {

        override fun fromKson(json: JsonObject): Requirement {
            return TeamLevelRequirement(
                level = json["level"]?.jsonPrimitive?.intOrNull ?: 0,
                comparison = when((json["comparison"]?.jsonPrimitive?.contentOrNull ?: "").uppercase()) {
                    "MINIMUM" -> LevelComparison.MINIMUM
                    "EXACT" -> LevelComparison.EXACT
                    "MAXIMUM" -> LevelComparison.MAXIMUM
                    else -> LevelComparison.MINIMUM
                }
            )
        }

    }

    override fun passesRequirement(player: ServerPlayerEntity): Boolean {
        when (comparison) {
            LevelComparison.MINIMUM -> {
                player.party().forEach {
                    if (it.level <= level) {
                        return false
                    }
                }
            }
            LevelComparison.EXACT -> {
                player.party().forEach {
                    if (it.level != level) {
                        return false
                    }
                }
            }
            LevelComparison.MAXIMUM -> {
                player.party().forEach {
                    if (it.level >= level) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun comparisonDescriber(): String {
        return when(comparison) {
            LevelComparison.MINIMUM -> "at least "
            LevelComparison.EXACT -> ""
            LevelComparison.MAXIMUM -> "lower than "
        }
    }

    override fun error(): LoreLike {
        return LoreLike.ofString(
            "<red>Your Pokemon must all be ${comparisonDescriber()}level ${level}!"
        )
    }

    override fun prompt(): LoreLike {
        return LoreLike.ofString(
            "<green>Your Pokemon must all be ${comparisonDescriber()}level ${level}!"
        )
    }

    override fun saveSpecifics(): MutableMap<String, JsonElement> {
        return mutableMapOf(
            "level" to JsonPrimitive(level)
        )
    }

}