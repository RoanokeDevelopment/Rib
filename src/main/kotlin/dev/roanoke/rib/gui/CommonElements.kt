package dev.roanoke.rib.gui

import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.screen.slot.SlotActionType
import dev.roanoke.rib.Rib

class CommonElements {

    companion object {

        fun backButton(title: String, gui: SimpleGui): GuiElementBuilder {
            return GuiElementBuilder.from(
                Items.ARROW.defaultStack.setCustomName(
                    Rib.Rib.parseText(title)
                )
            ).setCallback { x: Int, y: ClickType?, z: SlotActionType? ->
                gui.open()
            };
        }

    }

}