package dev.roanoke.rib.gui.settings.types

import dev.roanoke.rib.Rib
import dev.roanoke.rib.gui.settings.BaseSetting
import dev.roanoke.rib.gui.settings.SettingsManager
import dev.roanoke.rib.location.Location
import dev.roanoke.rib.location.LocationRegistry
import dev.roanoke.rib.utils.GuiUtils
import dev.roanoke.rib.utils.PaginatedSection
import dev.roanoke.rib.utils.SlotRange
import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class LocationsSetting(
    override val name: String,
    private val getter: () -> MutableList<Location>,
    private val setter: (MutableList<Location>) -> Unit
) : BaseSetting<MutableList<Location>>() {

    override var settingsManager: SettingsManager? = null

    override fun getValue(): MutableList<Location> = getter()

    override fun setValue(value: MutableList<Location>) {
        setter(value)
    }

    override var guiElement: GuiElementBuilder = GuiElementBuilder(Items.COMPASS)

    private fun getLocationLore(): List<Text> {
        return listOf(
            "Left click to teleport to Location",
            "Right click to remove Location"
        ).map { Rib.Rib.parseText(it) }
    }

    private fun openLocationsGui(player: ServerPlayerEntity) {
        val gui: SimpleGui = object : SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false) {
            override fun onClose() {
                settingsManager?.openMenu(player)
            }
        }
        gui.setTitle(Rib.Rib.parseText(name))

        val guiElements = getValue().map {
            GuiElementBuilder(Items.MAP)
                .setName(Rib.Rib.parseText(name))
                .setLore(getLocationLore())
                .setCallback { _, y: ClickType, _ ->
                    if (y.isLeft) {
                        it.teleportPlayer(player)
                    }
                    if (y.isRight) {
                        val newLocations = getValue().toMutableList()
                        newLocations.remove(it)
                        setValue(newLocations)
                        settingsManager?.save()
                    }
                    openLocationsGui(player)
                }
        }

        gui.setSlot(13, GuiElementBuilder(Items.CRAFTING_TABLE)
            .setName(Rib.Rib.parseText("<green>Add Current Location"))
            .setCallback { _, _, _ ->
                val newLocations = getValue()
                newLocations.add(LocationRegistry.fromEntity(player))
                setValue(newLocations)
                settingsManager?.save()
                openLocationsGui(player)
            })

        val dexSection = PaginatedSection(guiElements)
            .setSlotRanges(
                java.util.List.of(
                    SlotRange(19, 25),
                    SlotRange(28, 34),
                    SlotRange(37, 38),
                    SlotRange(42, 43)
                )
            )

        dexSection.applyToGui(gui)

        gui.setSlot(39, GuiElementBuilder.from(
            Items.ARROW.defaultStack.setCustomName(
                Text.literal("Previous Page")
            )
        ).setCallback { x: Int, y: ClickType?, z: SlotActionType? ->
            dexSection.decrementPage()
            dexSection.applyToGui(gui)
        })

        gui.setSlot(41, GuiElementBuilder.from(
            Items.ARROW.defaultStack.setCustomName(
                Text.literal("Next Page")
            )
        ).setCallback { x: Int, y: ClickType?, z: SlotActionType? ->
            dexSection.incremementPage()
            dexSection.applyToGui(gui)
        })

        GuiUtils.fillGUI(gui)

        gui.open()
    }

    override fun createGuiElement(player: ServerPlayerEntity): GuiElementBuilder {
        return guiElement
            .setName(Rib.Rib.parseText(name))
            .setLore(getLore())
            .setCallback { _, y: ClickType, _ ->
                openLocationsGui(player)
            }
    }

    override fun getLore(): List<Text> {
        val lore: MutableList<Text> = mutableListOf()
        lore.addAll(
            getDescriptionLore()
        )
        lore.add(
            Rib.Rib.parseText("Left click to manage Locations")
        )
        return lore
    }

}