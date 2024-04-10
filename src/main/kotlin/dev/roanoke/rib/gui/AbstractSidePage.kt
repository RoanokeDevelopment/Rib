package dev.roanoke.rib.gui

import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import dev.roanoke.rib.Rib
import dev.roanoke.rib.utils.GuiUtils
import dev.roanoke.rib.utils.PaginatedSection
import dev.roanoke.rib.utils.SlotRange

abstract class AbstractSidePage(
    open val player: ServerPlayerEntity,
    open val title: String = "Default Side Page Title",
    open val buttons: MutableList<GuiElementBuilder> = mutableListOf(),
    open val elements: MutableList<GuiElementBuilder> = mutableListOf(),
    open val gui: SimpleGui = SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false)
) {

    open val buttonIndices: List<Int> = listOf(10, 19, 28)

    abstract fun preOpen()

    fun open() {
        preOpen()
        gui.title = Rib.Rib.parseText(title)
        applyButtons()
        applyElements()
        GuiUtils.fillGUI(gui)
        gui.open()
    }

    fun applyButtons() {
        val buttonsIterator = buttons.iterator()
        for (index in buttonIndices) {
            if (buttonsIterator.hasNext()) {
                gui.setSlot(index, buttonsIterator.next())
            }
        }
    }

    fun applyElements() {
        val gymSection = PaginatedSection(elements)
            .setSlotRanges(
                listOf(
                    SlotRange(12, 16),
                    SlotRange(21, 25),
                    SlotRange(31, 33)
                )
            )

        gymSection.applyToGui(gui)

        gui.setSlot(30, GuiElementBuilder.from(
            Items.ARROW.defaultStack.setCustomName(
                Text.literal("Previous Page")
            )
        ).setCallback { x: Int, y: ClickType?, z: SlotActionType? ->
            gymSection.decrementPage()
            gymSection.applyToGui(gui)
        })

        gui.setSlot(34, GuiElementBuilder.from(
            Items.ARROW.defaultStack.setCustomName(
                Text.literal("Next Page")
            )
        ).setCallback { x: Int, y: ClickType?, z: SlotActionType? ->
            gymSection.incremementPage()
            gymSection.applyToGui(gui)
        })
    }

}