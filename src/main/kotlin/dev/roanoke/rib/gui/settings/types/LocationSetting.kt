package dev.roanoke.rib.gui.settings.types

import dev.roanoke.rib.gui.settings.Setting
import dev.roanoke.rib.gui.settings.SettingsManager
import dev.roanoke.rib.utils.Location
import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class LocationSetting(
    override val name: String,
    private val getter: () -> Location?,
    private val setter: (Location?) -> Unit
) : Setting<Location?> {

    override var settingsManager: SettingsManager? = null

    override fun getValue(): Location? = getter()

    override fun setValue(value: Location?) {
        setter(value)
    }

    override fun createGuiElement(player: ServerPlayerEntity): GuiElementBuilder {
        return GuiElementBuilder(Items.LEVER)
            .setName(Text.literal(name))
            .setLore(listOf(
                Text.literal("Left click to visit Location"),
                Text.literal("Right click to set to your current Location")
            ))
            .setCallback { _, y: ClickType, _ ->
                if (y.isLeft) {
                    getValue()?.teleportPlayer(player)
                }
                if (y.isRight) {
                    setValue(Location.fromEntity(player))
                }
                settingsManager?.openMenu(player)
            }
    }
}