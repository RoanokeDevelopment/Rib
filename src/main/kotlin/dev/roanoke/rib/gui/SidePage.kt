package dev.roanoke.rib.gui

import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity

class SidePage(
    override val player: ServerPlayerEntity,
    override val title: String = "Default Side Page Title",
    override val buttons: MutableList<GuiElementBuilder> = mutableListOf(),
    override val elements: MutableList<GuiElementBuilder> = mutableListOf(),
    override val gui: SimpleGui = SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false)
): AbstractSidePage(player, title, buttons, elements, gui) {
    override fun preOpen() {
        // pass
    }

}