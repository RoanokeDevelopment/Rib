package dev.roanoke.rib.requirements.types

import com.cobblemon.mod.common.util.party
import dev.roanoke.rib.gui.settings.SettingsManager
import dev.roanoke.rib.gui.settings.types.IntegerComparisonSetting
import dev.roanoke.rib.gui.settings.types.IntegerSetting
import dev.roanoke.rib.requirements.Requirement
import dev.roanoke.rib.requirements.RequirementFactory
import dev.roanoke.rib.utils.LoreLike
import kotlinx.serialization.json.*
import net.minecraft.server.network.ServerPlayerEntity

enum class IntegerComparison {
    MINIMUM,
    EXACT,
    MAXIMUM
}

class TeamLevelRequirement(
    var level: Int,
    var comparison: IntegerComparison
): Requirement(
    type = "TeamLevelRequirement"
) {

    companion object : RequirementFactory {

        override fun fromKson(json: JsonObject): Requirement {
            return TeamLevelRequirement(
                level = json["level"]?.jsonPrimitive?.intOrNull ?: 0,
                comparison = when((json["comparison"]?.jsonPrimitive?.contentOrNull ?: "").uppercase()) {
                    "MINIMUM" -> IntegerComparison.MINIMUM
                    "EXACT" -> IntegerComparison.EXACT
                    "MAXIMUM" -> IntegerComparison.MAXIMUM
                    else -> IntegerComparison.MINIMUM
                }
            )
        }

    }

    init {
        registerSettings()
    }

    override fun registerSettings() {
        settings = SettingsManager(this)
        settings.addSettings(
            IntegerSetting("Level", { level }, { level = it }),
            IntegerComparisonSetting("Comparison Type", { comparison }, { comparison = it })
        )
    }

    override fun passesRequirement(player: ServerPlayerEntity): Boolean {
        when (comparison) {
            IntegerComparison.MINIMUM -> {
                player.party().forEach {
                    if (it.level < level) {
                        return false
                    }
                }
            }
            IntegerComparison.EXACT -> {
                player.party().forEach {
                    if (it.level != level) {
                        return false
                    }
                }
            }
            IntegerComparison.MAXIMUM -> {
                player.party().forEach {
                    if (it.level > level) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun comparisonDescriber(): String {
        return when(comparison) {
            IntegerComparison.MINIMUM -> "at least "
            IntegerComparison.EXACT -> ""
            IntegerComparison.MAXIMUM -> "lower than "
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

    override fun save() {
    }

}