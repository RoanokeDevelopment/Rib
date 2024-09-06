package dev.roanoke.rib.gui.settings.types

import dev.roanoke.rib.Rib
import dev.roanoke.rib.gui.settings.BaseSetting
import dev.roanoke.rib.gui.settings.SettingsManager
import dev.roanoke.rib.utils.Location
import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

class Vec3dSetting(
    override val name: String,
    private val getter: () -> Vec3d?,
    private val setter: (Vec3d?) -> Unit
) : BaseSetting<Vec3d?>() {

    override var settingsManager: SettingsManager? = null

    override fun getValue(): Vec3d? = getter()

    override fun setValue(value: Vec3d?) {
        setter(value)
    }

    override var guiElement: GuiElementBuilder = GuiElementBuilder(Items.COMPASS)

    override fun createGuiElement(player: ServerPlayerEntity): GuiElementBuilder {
        return guiElement
            .setName(Rib.Rib.parseText(name))
            .setLore(getLore())
            .setCallback { _, y: ClickType, _ ->
                if (y.isRight) {
                    setValue(player.pos)
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
        getValue()?.let { vec ->
            lore.add(
                Rib.Rib.parseText("Currently: x${vec.x}, y${vec.y}, z${vec.z}")
            )
        }
        lore.add(
            Rib.Rib.parseText("Right click to set to your current location!")
        )
        return lore
    }

}