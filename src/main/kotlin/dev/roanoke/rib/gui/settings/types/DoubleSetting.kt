package dev.roanoke.rib.gui.settings.types

import dev.roanoke.rib.Rib
import dev.roanoke.rib.gui.settings.BaseSetting
import dev.roanoke.rib.gui.settings.SettingsManager
import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.AnvilInputGui
import net.minecraft.item.Items
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class DoubleSetting(
    override val name: String,
    private val getter: () -> Double,
    private val setter: (Double) -> Unit
) : BaseSetting<Double>() {

    override var settingsManager: SettingsManager? = null

    override var description: String = ""

    override fun getValue(): Double = getter()

    override fun setValue(value: Double) {
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

    private fun openAnvilGui(player: ServerPlayerEntity, onInput: (Double) -> Unit) {
        val gui: AnvilInputGui = object : AnvilInputGui(player, true) {

            override fun onClose() {
                settingsManager?.openMenu(player)
            }

            override fun onInput(input: String) {
                if (input.toDoubleOrNull() != null) {
                    this.setSlot(2, GuiElement(
                        Items.SLIME_BALL.defaultStack.setCustomName(
                        Text.literal("Left click to set $name"))
                            ) { index: Int, clickType: ClickType?, actionType: SlotActionType? ->
                                this.input.toDoubleOrNull()?.let {
                                    onInput(it)
                                }
                            })
                } else {
                    this.setSlot(2, GuiElement(
                        Items.REDSTONE.defaultStack.setCustomName(
                        Text.literal("$name must be an Double!"))
                            ) { index: Int, clickType: ClickType?, actionType: SlotActionType? ->
                                player.sendMessage(Text.literal("This setting must be a double! (i.e. a decimal number, 1.0, 0.4, 42.0, etc)"))
                            })
                }
            }

        }

        gui.setDefaultInputValue("${getValue()}")

        gui.title = Text.literal("Set $name")

        gui.setSlot(2, GuiElement(
            Items.SLIME_BALL.defaultStack.setCustomName(
                Text.literal("Left click to set $name")
            )
        ) { index: Int, clickType: ClickType?, actionType: SlotActionType? ->
            gui.input.toDoubleOrNull()?.let {
                onInput(it)
            }
        })

        gui.open()
    }
}