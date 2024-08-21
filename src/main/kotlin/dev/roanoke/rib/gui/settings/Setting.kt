package dev.roanoke.rib.gui.settings

import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.server.network.ServerPlayerEntity

interface Setting<T> {

    val name: String
    var settingsManager: SettingsManager?

    fun getValue(): T
    fun setValue(value: T)
    fun createGuiElement(player: ServerPlayerEntity): GuiElementBuilder

}