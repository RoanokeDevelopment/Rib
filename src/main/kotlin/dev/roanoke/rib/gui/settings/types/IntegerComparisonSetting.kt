package dev.roanoke.rib.gui.settings.types

import dev.roanoke.rib.Rib
import dev.roanoke.rib.gui.settings.BaseSetting
import dev.roanoke.rib.gui.settings.SettingsManager
import dev.roanoke.rib.requirements.types.IntegerComparison
import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.AnvilInputGui
import net.minecraft.item.Items
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class IntegerComparisonSetting(
    override val name: String,
    private val getter: () -> IntegerComparison,
    private val setter: (IntegerComparison) -> Unit
) : BaseSetting<IntegerComparison>() {

    override var settingsManager: SettingsManager? = null

    override var description: String = ""

    override fun getValue(): IntegerComparison = getter()

    override fun setValue(value: IntegerComparison) {
        setter(value)
    }

    override fun createGuiElement(player: ServerPlayerEntity): GuiElementBuilder {
        return guiElement
            .setName(Rib.Rib.parseText(name))
            .setLore(getLore())
            .setCallback { _, _, _ ->
                openSelectionMenu(player)
            }
    }

    fun openSelectionMenu(player: ServerPlayerEntity) {

        val cGui = Rib.GUIs.getGui("generic_manage") ?: return

        val gui = cGui.getGui(
            player = player,
            elements = mapOf(
                "X" to IntegerComparison.values().map {
                    GuiElementBuilder(Items.HONEYCOMB)
                        .setName(Rib.Rib.parseText(it.name))
                        .setLore(listOf(Rib.Rib.parseText(
                            "Left click to set Comparison Type"
                        )))
                        .setCallback { _, _, _ ->
                            setValue(it)
                            settingsManager?.save()
                            settingsManager?.openMenu(player)
                        }
                }
            ),
            onClose = { p -> settingsManager?.openMenu(p) }
        )

        gui.title = Rib.Rib.parseText("Choose Comparison Type")

        gui.open()

    }

    override fun getLore(): List<Text> {
        val lore: MutableList<Text> = mutableListOf()
        lore.addAll(
            getDescriptionLore()
        )
        lore.add(
            Rib.Rib.parseText("Currently: <green>${getValue()}<reset>")
        )
        lore.add(
            Rib.Rib.parseText("Click to edit!")
        )
        return lore
    }

}