package dev.roanoke.rib.gui.settings.types

import dev.roanoke.rib.Rib
import dev.roanoke.rib.gui.settings.BaseSetting
import dev.roanoke.rib.gui.settings.SettingsManager
import dev.roanoke.rib.requirements.Requirement
import dev.roanoke.rib.requirements.RequirementRegistry
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class RequirementsSetting(
    override val name: String,
    private val getter: () -> MutableList<Requirement>,
    private val setter: (MutableList<Requirement>) -> Unit
): BaseSetting<MutableList<Requirement>>() {

    override var settingsManager: SettingsManager? = null

    override var description: String = ""

    override fun getValue(): MutableList<Requirement> = getter()

    override fun setValue(value: MutableList<Requirement>) {
        setter(value)
    }

    override fun createGuiElement(player: ServerPlayerEntity): GuiElementBuilder {
        return guiElement
            .setName(Rib.Rib.parseText(name))
            .setLore(getLore())
            .setCallback { _, _, _ ->
                openManageRequirements(player)
            }
    }

    private fun addRequirementMenu(player: ServerPlayerEntity) {

        val cGui = Rib.GUIs.getGui("generic_paginated") ?: return

        val gui = cGui.getGui(
            player = player,
            elements = mapOf(
                "X" to RequirementRegistry.getRequirements().map {
                    it.getGuiElement()
                        .setCallback { _, _, _ ->
                            val requirements = getValue()
                            requirements.add(it)
                            setValue(requirements)
                            settingsManager?.save()
                            openManageRequirements(player)
                        }
                }
            ),
            onClose = { p ->
                openManageRequirements(p)
            }
        )

        gui.title = Rib.Rib.parseText("Add New Requirement")

        gui.open()

    }

    private fun openManageRequirements(player: ServerPlayerEntity) {

        val cGui = Rib.GUIs.getGui("generic_manage") ?: return

        val gui = cGui.getGui(
            player = player,
            elements = mapOf(
                "B" to listOf(
                    GuiElementBuilder(Items.ANVIL)
                        .setName(Rib.Rib.parseText("<green>Add Requirement"))
                        .setCallback { _, _, _ ->
                            addRequirementMenu(player)
                        }
                ),
                "X" to getValue().map {
                    it.getGuiElement()
                        .setCallback { _, _, _ ->
                            it.openMenu(player, onClose = {
                                p -> openManageRequirements(p)
                            })
                        }
                }
            ),
            onClose = { p ->
                settingsManager?.openMenu(p)
            }
        )

        gui.title = Rib.Rib.parseText("Manage Requirements")

        gui.open()

    }

    override fun getLore(): List<Text> {
        val lore: MutableList<Text> = mutableListOf()
        lore.addAll(
            getDescriptionLore()
        )
        lore.add(
            Rib.Rib.parseText("<gray>Click to edit / add new Requirements!")
        )
        return lore
    }

}