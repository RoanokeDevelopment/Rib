package dev.roanoke.rib.gui.settings.types

import dev.roanoke.rib.Rib
import dev.roanoke.rib.gui.settings.BaseSetting
import dev.roanoke.rib.gui.settings.Setting
import dev.roanoke.rib.gui.settings.SettingsManager
import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.AnvilInputGui
import net.minecraft.item.Items
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class StringSetting(
    override val name: String,
    private val getter: () -> String,
    private val setter: (String) -> Unit
) : BaseSetting<String>() {

    override var settingsManager: SettingsManager? = null

    override var description: String = ""


    override fun getValue(): String = getter()

    override fun setValue(value: String) {
        setter(value)
    }

    override fun createGuiElement(player: ServerPlayerEntity): GuiElementBuilder {
        return guiElement
            .setName(Rib.Rib.parseText(name))
            .setLore(getLore())
            .setCallback { _, _, _ ->
                openAnvilGui(player) { newValue ->
                    setValue(newValue)
                    settingsManager?.save()
                    settingsManager?.openMenu(player)
                }
            }
    }

    private fun openAnvilGui(player: ServerPlayerEntity, onInput: (String) -> Unit) {
        val gui: AnvilInputGui = object : AnvilInputGui(player, true) {
            override fun onClose() {
                settingsManager?.openMenu(player)
            }
        }

        gui.title = Text.literal("Set $name")

        gui.setSlot(2, GuiElement(
            Items.SLIME_BALL.defaultStack.setCustomName(
                Text.literal("Left click to set $name")
            )
        ) { index: Int, clickType: ClickType?, actionType: SlotActionType? ->
            onInput(gui.input)
        })

        gui.open()
    }

    override fun getLore(): List<Text> {
        val lore: MutableList<Text> = mutableListOf()
        lore.addAll(
            getDescriptionLore()
        )
        lore.add(
            Rib.Rib.parseText("Currently: ${getValue()}")
        )
        lore.add(
            Rib.Rib.parseText("Click to edit!")
        )
        return lore
    }

}