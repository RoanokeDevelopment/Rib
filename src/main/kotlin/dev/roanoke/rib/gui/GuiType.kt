package dev.roanoke.rib.gui

import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandlerType

enum class GuiType {
    G9X1,
    G9X3,
    G9X4,
    G9X5,
    G9X6,
    G3X3;

    companion object {
        fun getScreenHandlerType(guiType: GuiType): ScreenHandlerType<*> {
            return when (guiType) {
                G9X1 -> ScreenHandlerType.GENERIC_9X1
                G9X3 -> ScreenHandlerType.GENERIC_9X3
                G9X4 -> ScreenHandlerType.GENERIC_9X4
                G9X5 -> ScreenHandlerType.GENERIC_9X5
                G9X6 -> ScreenHandlerType.GENERIC_9X6
                G3X3 -> ScreenHandlerType.GENERIC_3X3
            }
        }
    }

}
