package dev.roanoke.rib.gui.settings

import dev.roanoke.rib.Rib
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.item.Items
import net.minecraft.text.Text

abstract class BaseSetting<T>: Setting<T> {

    override var settingsManager: SettingsManager? = null

    override var description: String = ""

    open var guiElement: GuiElementBuilder = GuiElementBuilder(Items.PAPER)

    fun getDescriptionLore(): List<Text> {
        val descriptionList: MutableList<Text> = mutableListOf()
        description.split("\n").forEach {
            descriptionList.add(
                Rib.Rib.parseText(it)
            )
        }
        return descriptionList
    }

    abstract fun getLore(): List<Text>

}
