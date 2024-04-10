package dev.roanoke.rib.gui

import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity

class RewardsSidePage(
    override val player: ServerPlayerEntity,
    override val title: String = "Manage Rewards",
    val backButton: GuiElementBuilder,
    override val buttons: MutableList<GuiElementBuilder> = mutableListOf(),
    override val elements: MutableList<GuiElementBuilder> = mutableListOf(),
    override val gui: SimpleGui = SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false))
    : AbstractSidePage(player, title, buttons, elements) {

    override val buttonIndices: List<Int> = listOf(19, 28)
    override fun preOpen() {
        gui.setSlot(10, backButton)
    }

}