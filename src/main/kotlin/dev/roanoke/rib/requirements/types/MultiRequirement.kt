package dev.roanoke.rib.requirements.types

import dev.roanoke.rib.gui.settings.SettingsManager
import dev.roanoke.rib.gui.settings.types.StringSetting
import dev.roanoke.rib.gui.settings.types.RequirementsSetting
import dev.roanoke.rib.requirements.Requirement
import dev.roanoke.rib.requirements.RequirementFactory
import dev.roanoke.rib.requirements.RequirementRegistry
import dev.roanoke.rib.utils.LoreLike
import kotlinx.serialization.json.*
import net.minecraft.server.network.ServerPlayerEntity

class MultiRequirement(
    var requirements: MutableList<Requirement>,
    var prompt: String
): Requirement("MultiRequirement") {

    companion object : RequirementFactory {

        override fun fromKson(json: JsonObject): Requirement {
            return MultiRequirement(
                requirements = json["requirements"]?.jsonArray?.map { RequirementRegistry.getRequirement(it.jsonObject) }?.toMutableList() ?: mutableListOf<Requirement>(),
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
            StringSetting("Prompt", { prompt }, { prompt = it }),
            RequirementsSetting("Requirements", { requirements }, { requirements = it })
        )
    }

    override fun passesRequirement(player: ServerPlayerEntity): Boolean {
        return requirements.all { it.passesRequirement(player) }
    }

    override fun error(): LoreLike {
        return LoreLike.ofString("<red>$prompt")
    }

    override fun prompt(): LoreLike {
        return LoreLike.ofString(prompt)
    }

    override fun saveSpecifics(): MutableMap<String, JsonElement> {
        return mutableMapOf(
            "requirements" to JsonArray(requirements.map { it.toKson() }),
            "prompt" to JsonPrimitive(prompt)
        )
    }

    override fun save() {

    }

}