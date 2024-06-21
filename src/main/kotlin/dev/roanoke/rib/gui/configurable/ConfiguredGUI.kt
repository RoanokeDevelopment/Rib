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

class ConfiguredGUI(
    val id: String,
    val guiType: GuiType,
    val guiList: List<String>,
    val keys: Map<String, ItemBuilder>
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
        Rib.LOGGER.info("Looking for key ${key} in ConfiguredGUI Keys...")
        Rib.LOGGER.info("Keys available: ")

        keys.forEach {
            Rib.LOGGER.info(" - ${it.key} : ${it.value.toString()}")
        }

        val item = keys[key] ?: ItemBuilder("minecraft:dirt")

        return GuiElementBuilder.from(item.build())
    }

    fun getGui(player: ServerPlayerEntity, elements: Map<String, List<GuiElementBuilder>>): SimpleGui {
        val gui = SimpleGui(GuiType.getScreenHandlerType(guiType), player, false)

        Rib.LOGGER.info("Getting GUI: $id")
        Rib.LOGGER.info("$guiList")

        var elementIndex = 0;
        for ((index, key) in guiList.withIndex()) {
            Rib.LOGGER.info("\n\nIndex: $index\nKey: $key")
            if (elements.containsKey(key)) {
                Rib.LOGGER.info("Elements contained key ($key)")
                if (elements[key]!!.size > elementIndex) {
                    Rib.LOGGER.info("Element size (${elements[key]!!.size}) bigger than element index ($elementIndex)")
                    gui.setSlot(index, elements[key]!![elementIndex])
                    elementIndex++
                    Rib.LOGGER.info("So placing element and increasing index")
                } else {
                    Rib.LOGGER.info("Element size (${elements[key]!!.size}) NOT bigger than element index ($elementIndex)")
                    // COULD ADD A SEPARATE PLACEHOLDER ITEM VALUE HERE
                    Rib.LOGGER.info("So placing nothing.")
                }
            } else {
                Rib.LOGGER.info("Elements didn't contain key ($key), using fallback items")
                gui.setSlot(index, getGuiElementFromKey(key))
            }
        }

        return gui;
    }

}