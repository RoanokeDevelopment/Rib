package dev.roanoke.rib.gui

import dev.roanoke.rib.utils.PaginatedSection
import dev.roanoke.rib.utils.SlotRange
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.text.Text
import kotlin.math.ceil

class MultiElement(
    val guiElements: List<GuiElementBuilder>,
    val slotRanges: List<SlotRange> = listOf(),
    val fillItem: GuiElementBuilder = GuiElementBuilder(
        Items.GRAY_STAINED_GLASS_PANE).setName(Text.literal("")),
    val backButtonSlot: Int = -1,
    val forwardButtonSlot: Int = -1
) {

    var currentPage: Int = 1

    fun getItemsPerPage(): Int {
        var itemsPerPage = 0
        for (range in this.slotRanges) {
            itemsPerPage += (range.end - range.start + 1)
        }
        return itemsPerPage
    }

    fun applyToGui(gui: SimpleGui) {
        val itemsPerPage = getItemsPerPage()

        var startingIndex = (this.currentPage - 1) * itemsPerPage

        for (range in this.slotRanges) {
            for (slot in range.start..range.end) {
                if (startingIndex < guiElements.size) {
                    gui.setSlot(slot, guiElements[startingIndex])
                } else {
                    gui.setSlot(slot, fillItem)
                }
                startingIndex++
            }
        }
    }

    fun incremementPage() {
        val totalPages =
            ceil(guiElements.size.toDouble() / this.getItemsPerPage()).toInt()
        currentPage++
        if (this.currentPage > totalPages) {
            currentPage--
        }
    }

    fun decrementPage() {
        currentPage--
        if (this.currentPage < 1) {
            this.currentPage = 1
        }
    }
}