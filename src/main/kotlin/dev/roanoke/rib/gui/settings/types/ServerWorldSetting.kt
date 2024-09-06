package dev.roanoke.rib.gui.settings.types

import dev.roanoke.rib.Rib
import dev.roanoke.rib.gui.settings.BaseSetting
import dev.roanoke.rib.gui.settings.SettingsManager
import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

class ServerWorldSetting(
    override val name: String,
    private val getter: () -> ServerWorld?,
    private val setter: (ServerWorld?) -> Unit
) : BaseSetting<ServerWorld?>() {

    override var settingsManager: SettingsManager? = null

    override fun getValue(): ServerWorld? = getter()

    override fun setValue(value: ServerWorld?) {
        setter(value)
    }

    override var guiElement: GuiElementBuilder = GuiElementBuilder(Items.COMPASS)

    override fun createGuiElement(player: ServerPlayerEntity): GuiElementBuilder {
        return guiElement
            .setName(Rib.Rib.parseText(name))
            .setLore(getLore())
            .setCallback { _, y: ClickType, _ ->
                if (y.isRight) {
                    setValue(player.serverWorld)
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
        getValue()?.let { world ->
            lore.add(
                Rib.Rib.parseText("Currently: ${world.registryKey.value.toString()}")
            )
        }
        lore.add(
            Rib.Rib.parseText("Right click to set to your current World!")
        )
        return lore
    }

}