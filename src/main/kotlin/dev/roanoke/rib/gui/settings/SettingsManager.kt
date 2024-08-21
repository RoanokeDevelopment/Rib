package dev.roanoke.rib.gui.settings

import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.server.network.ServerPlayerEntity

class SettingsManager(private val configurableGui: ConfigurableGui) {

    private val settings: MutableList<Setting<*>> = mutableListOf()

    fun addSetting(setting: Setting<*>) {
        setting.settingsManager = this
        settings.add(setting)
    }

    fun openMenu(player: ServerPlayerEntity) {
        configurableGui.openMenu(player)
    }

    fun save() {
        configurableGui.save()
    }

    fun addSettings(vararg newSettings: Setting<*>) {
        newSettings.forEach {
            it.settingsManager = this
        }
        settings.addAll(newSettings)
    }

    fun getGuiElements(player: ServerPlayerEntity): List<GuiElementBuilder> {
        return settings.map { it.createGuiElement(player) }
    }

}