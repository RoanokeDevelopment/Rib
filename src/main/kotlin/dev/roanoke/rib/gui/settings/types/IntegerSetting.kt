package dev.roanoke.rib.gui.settings.types

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

class IntegerSetting(
    override val name: String,
    private val getter: () -> Int,
    private val setter: (Int) -> Unit
) : Setting<Int> {

    override var settingsManager: SettingsManager? = null

    override fun getValue(): Int = getter()

    override fun setValue(value: Int) {
        setter(value)
    }

    override fun createGuiElement(player: ServerPlayerEntity): GuiElementBuilder {
        return GuiElementBuilder(Items.PAPER)
            .setName(Text.literal(name))
            .setLore(listOf(
                Text.literal("Current: ${getValue()}"),
                Text.literal("Click to edit")
            ))
            .setCallback { _, _, _ ->
                openAnvilGui(player) { newValue ->
                        setValue(newValue)
                        settingsManager?.save()
                        settingsManager?.openMenu(player)
                }
            }
    }

    private fun openAnvilGui(player: ServerPlayerEntity, onInput: (Int) -> Unit) {
        val gui: AnvilInputGui = object : AnvilInputGui(player, true) {

            override fun onClose() {
                settingsManager?.openMenu(player)
            }

            override fun onInput(input: String) {
                if (input.toIntOrNull() != null) {
                    this.setSlot(2, GuiElement(
                        Items.SLIME_BALL.defaultStack.setCustomName(
                        Text.literal("Left click to set $name"))
                            ) { index: Int, clickType: ClickType?, actionType: SlotActionType? ->
                                this.input.toIntOrNull()?.let {
                                    onInput(it)
                                }
                            })
                } else {
                    this.setSlot(2, GuiElement(
                        Items.REDSTONE.defaultStack.setCustomName(
                        Text.literal("$name must be an integer!"))
                            ) { index: Int, clickType: ClickType?, actionType: SlotActionType? ->
                                player.sendMessage(Text.literal("This setting must be an integer!"))
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
            gui.input.toIntOrNull()?.let {
                onInput(it)
            }
        })

        gui.open()
    }
}
