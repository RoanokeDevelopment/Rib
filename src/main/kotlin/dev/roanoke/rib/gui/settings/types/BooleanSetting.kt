package dev.roanoke.rib.gui.settings.types

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
) : Setting<Boolean> {

    override var settingsManager: SettingsManager? = null

    override fun getValue(): Boolean = getter()

    override fun setValue(value: Boolean) {
        setter(value)
    }

    override fun createGuiElement(player: ServerPlayerEntity): GuiElementBuilder {
        return GuiElementBuilder(Items.LEVER)
            .setName(Text.literal(name))
            .setLore(listOf(
                Text.literal("Current: ${if (getValue()) "Enabled" else "Disabled"}"),
                Text.literal("Click to toggle")
            ))
            .setCallback { _, _, _ ->
                setValue(!getValue())
                player.sendMessage(Text.literal("$name set to ${if (getValue()) "Enabled" else "Disabled"}"))
                settingsManager?.openMenu(player)
            }
    }
}