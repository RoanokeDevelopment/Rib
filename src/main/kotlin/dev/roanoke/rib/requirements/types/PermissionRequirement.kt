package dev.roanoke.rib.requirements.types

import dev.roanoke.rib.Rib
import dev.roanoke.rib.gui.settings.SettingsManager
import dev.roanoke.rib.gui.settings.types.StringSetting
import dev.roanoke.rib.requirements.Requirement
import dev.roanoke.rib.requirements.RequirementFactory
import dev.roanoke.rib.utils.LoreLike
import kotlinx.serialization.json.*
import net.minecraft.server.network.ServerPlayerEntity

class PermissionRequirement(
    var permission: String,
    var prompt: String
): Requirement("PermissionRequirement", "Permission Requirement") {

    companion object : RequirementFactory {

        override fun fromKson(json: JsonObject): Requirement {
            return PermissionRequirement(
                permission = json["permission"]?.jsonPrimitive?.contentOrNull ?: "",
                prompt = json["prompt"]?.jsonPrimitive?.contentOrNull ?: ""
            )
        }

    }

    init {
        registerSettings()
    }

    override fun registerSettings() {
        settings = SettingsManager(this)
        settings.addSettings(
            StringSetting("Prompt", { prompt }, { prompt = it })
                .also {
                    it.description = "\n<gray>The display message a player will see so they know what they need.\n"
                },
            StringSetting("Permission", { permission }, { permission = it })
                .also {
                    it.description = "\n<gray>The permission node that's required\n<gray>E.g. 'ggyms.command.reload'\n"
                }
        )
    }

    override fun description(): List<String> {
        return listOf(
            "Require a player to have a certain permission node",
            "Note - you <bold>need<reset> the permission node, even if you're OP'd"
        )
    }

    override fun passesRequirement(player: ServerPlayerEntity): Boolean {
        return Rib.perm.hasPermission(player, permission)
    }

    override fun error(): LoreLike {
        return LoreLike.ofString("<red>${prompt}")
    }

    override fun prompt(): LoreLike {
        return LoreLike.ofString(prompt)
    }

    override fun saveSpecifics(): MutableMap<String, JsonElement> {
        return mutableMapOf(
            "permission" to JsonPrimitive(permission),
            "prompt" to JsonPrimitive(prompt)
        )
    }

    override fun save() {
    }

}