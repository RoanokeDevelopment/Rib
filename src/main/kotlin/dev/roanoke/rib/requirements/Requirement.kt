package dev.roanoke.rib.requirements

import dev.roanoke.rib.Rib
import dev.roanoke.rib.gui.settings.ConfigurableGui
import dev.roanoke.rib.gui.settings.SettingsManager
import dev.roanoke.rib.utils.LoreLike
import eu.pb4.sgui.api.elements.GuiElementBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

abstract class Requirement(
    val type: String,
    val name: String = type
): ConfigurableGui() {

    lateinit var settings: SettingsManager

    abstract fun registerSettings()

    abstract fun description(): List<String>

    open fun lore(): List<Text> {
        val lore = mutableListOf<String>("")
        lore.addAll(description())
        lore.add("")
        return lore.map { Rib.Rib.parseText(it) }
    }

    open fun getGuiElement(): GuiElementBuilder {
        return GuiElementBuilder(Items.IRON_CHESTPLATE)
            .setName(Rib.Rib.parseText(name))
            .setLore(lore())
    }

    override fun openMenu(player: ServerPlayerEntity, onClose: (ServerPlayerEntity) -> Unit) {

        val cGui = Rib.GUIs.getGui("generic_manage") ?: return

        val gui = cGui.getGui(
            player = player,
            elements = mapOf(
                "X" to settings.getGuiElements(player)
            ),
            onClose = onClose
        )

        gui.title = Rib.Rib.parseText("Edit $name")

        gui.open()

    }

    abstract fun passesRequirement(player: ServerPlayerEntity): Boolean

    abstract fun error(): LoreLike

    abstract fun prompt(): LoreLike

    abstract fun saveSpecifics(): MutableMap<String, JsonElement>

    fun toKson(): JsonObject {
        return JsonObject(
            saveSpecifics().also {
                it["type"] = JsonPrimitive(type)
            }
        )
    }

}