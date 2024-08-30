package dev.roanoke.rib.gui.settings.types

import dev.roanoke.rib.Rib
import dev.roanoke.rib.gui.settings.BaseSetting
import dev.roanoke.rib.gui.settings.Setting
import dev.roanoke.rib.gui.settings.SettingsManager
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class BooleanSetting(
    override val name: String,
    private val getter: () -> Boolean,
    private val setter: (Boolean) -> Unit
) : BaseSetting<Boolean>() {

    override var settingsManager: SettingsManager? = null

    override var guiElement: GuiElementBuilder = GuiElementBuilder(Items.LEVER)

    override fun getValue(): Boolean = getter()

    override fun setValue(value: Boolean) {
        setter(value)
    }

    override fun createGuiElement(player: ServerPlayerEntity): GuiElementBuilder {
        return guiElement
            .setName(Rib.Rib.parseText(name))
            .setLore(getLore())
            .setCallback { _, _, _ ->
                setValue(!getValue())
                settingsManager?.save()
                player.sendMessage(Text.literal("$name set to ${if (getValue()) "Enabled" else "Disabled"}"))
                settingsManager?.openMenu(player)
            }
    }

    override fun getLore(): List<Text> {
        val lore: MutableList<Text> = mutableListOf()
        lore.addAll(
            getDescriptionLore()
        )
        lore.add(
            Rib.Rib.parseText("Currently: ${if (getValue()) "Enabled" else "Disabled"}")
        )
        lore.add(
            Rib.Rib.parseText("Click to toggle!")
        )
        return lore
    }

}