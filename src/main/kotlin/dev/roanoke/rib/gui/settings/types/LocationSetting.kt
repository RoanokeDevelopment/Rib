package dev.roanoke.rib.gui.settings.types

import dev.roanoke.rib.Rib
import dev.roanoke.rib.gui.settings.BaseSetting
import dev.roanoke.rib.gui.settings.SettingsManager
import dev.roanoke.rib.location.Location
import dev.roanoke.rib.location.LocationRegistry
import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class LocationSetting(
    override val name: String,
    private val getter: () -> Location?,
    private val setter: (Location?) -> Unit
) : BaseSetting<Location?>() {

    override var settingsManager: SettingsManager? = null

    override fun getValue(): Location? = getter()

    override fun setValue(value: Location?) {
        setter(value)
    }

    override var guiElement: GuiElementBuilder = GuiElementBuilder(Items.MAP)

    override fun createGuiElement(player: ServerPlayerEntity): GuiElementBuilder {
        return guiElement
            .setName(Rib.Rib.parseText(name))
            .setLore(getLore())
            .setCallback { _, y: ClickType, _ ->
                if (y.isLeft) {
                    getValue()?.teleportPlayer(player)
                }
                if (y.isRight) {
                    setValue(LocationRegistry.fromEntity(player))
                    settingsManager?.save()
                }
                settingsManager?.openMenu(player)
            }
    }

    override fun getLore(): List<Text> {
        val lore: MutableList<Text> = mutableListOf()
        lore.addAll(
            getDescriptionLore()
        )
        lore.add(
            Rib.Rib.parseText("Left click to visit Location!")
        )
        lore.add(
            Rib.Rib.parseText("Right click to set to your current LocatioN!")
        )
        return lore
    }

}