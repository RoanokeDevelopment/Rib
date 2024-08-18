package dev.roanoke.rib.gui.configurable

import dev.roanoke.rib.Rib
import dev.roanoke.rib.cereal.JsonConv
import dev.roanoke.rib.gui.GuiType
import dev.roanoke.rib.utils.ItemBuilder
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import kotlinx.serialization.json.*
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import java.nio.file.Path

open class ConfiguredGUI(
    val id: String,
    val guiType: GuiType,
    val guiList: List<String>,
    val keys: Map<String, ItemBuilder>,
    val paginated: Boolean = false,
    val backKey: String = "<",
    val forwardKey: String = ">",
    val paginatedElement: String = "X"
) {

    companion object {
        fun fromJson(json: JsonObject, items: MutableMap<String, ItemBuilder>): ConfiguredGUI? {
            val id: String = json["id"]?.jsonPrimitive?.content ?: return null

            var guiType = GuiType.G9X5;
            try {
                guiType = enumValueOf<GuiType>(json["gui_type"]?.jsonPrimitive?.content ?: return null)
            } catch (e: IllegalArgumentException) {
                Rib.LOGGER.error("Failed to load Configured GUI from Json: ", e)
                return null;
            }

            val guiList: List<String> = json["gui"]?.jsonArray?.flatMap {
                it.jsonPrimitive.content.asIterable().map { char -> char.toString() }
            } ?: return null

            val keys: MutableMap<String, ItemBuilder> = json["keys"]?.jsonObject?.mapNotNull { (key, valueJsonElement) ->
                val itemBuilder: ItemBuilder? = when {
                    valueJsonElement is JsonPrimitive -> items[valueJsonElement.content]
                    valueJsonElement is JsonObject -> ItemBuilder.fromJson(JsonConv.kotlinJsonToGsonJson(valueJsonElement))
                    else -> null
                }

                itemBuilder?.let { key to it }
            }?.toMap(mutableMapOf()) ?: return null

            return ConfiguredGUI(id, guiType, guiList, keys)
        }
    }


    fun getGuiElementFromKey(key: String): GuiElementBuilder {

        val item = keys[key] ?: ItemBuilder("minecraft:dirt")

        return GuiElementBuilder.from(item.build())
    }

    fun getGui(player: ServerPlayerEntity, elements: Map<String, List<GuiElementBuilder>>, onClose: (ServerPlayerEntity) -> Unit = {}): SimpleGui {
         val gui = object : SimpleGui(GuiType.getScreenHandlerType(guiType), player, false) {
            override fun onClose() {
                onClose(player)
            }
        }

        val elementIndex: MutableMap<String, Int> = mutableMapOf();
        elements.keys.forEach { elementIndex[it] = 0 }
        keys.keys.forEach { elementIndex[it] = 0 }

        for ((index, key) in guiList.withIndex()) {
            if (elements.containsKey(key)) {
                if (elements[key]!!.size > elementIndex[key]!!) {
                    gui.setSlot(index, elements[key]!![elementIndex[key]!!])
                    elementIndex[key] = elementIndex[key]!! + 1
                } else {
                    gui.setSlot(index, getGuiElementFromKey(key))
                }
            } else {
                gui.setSlot(index, getGuiElementFromKey(key))
            }
        }

        return gui;
    }

}