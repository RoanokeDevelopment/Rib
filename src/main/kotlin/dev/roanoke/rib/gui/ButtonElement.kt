package dev.roanoke.rib.gui

import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.server.network.ServerPlayerEntity

interface ButtonElement {

    fun getButton(player: ServerPlayerEntity): GuiElementBuilder

}