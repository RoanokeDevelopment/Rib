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
import kotlin.math.ceil

open class ConfiguredGUI(
    val id: String,
    val guiType: GuiType,
    val guiList: List<String>,
    val keys: Map<String, ItemBuilder>,
    val title: String = "",
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

            val title = json["title"]?.jsonPrimitive?.contentOrNull ?: ""
            val paginated = json["paginated"]?.jsonPrimitive?.booleanOrNull ?: false
            val backKey = json["backKey"]?.jsonPrimitive?.contentOrNull ?: "<"
            val forwardKey = json["forwardKey"]?.jsonPrimitive?.contentOrNull ?: ">"
            val paginatedElement = json["paginatedElement"]?.jsonPrimitive?.contentOrNull ?: "X"

            return ConfiguredGUI(
                id = id,
                guiType = guiType,
                guiList = guiList,
                keys = keys,
                title = title,
                paginated = paginated,
                backKey = backKey,
                forwardKey = forwardKey,
                paginatedElement = paginatedElement
            )
        }
    }


    fun getGuiElementFromKey(key: String): GuiElementBuilder {

        val item = keys[key] ?: ItemBuilder("minecraft:dirt")

        return GuiElementBuilder.from(item.build())
    }

    fun getPaginatedElementCount(elements: Map<String, List<GuiElementBuilder>>): Int {
        return elements[paginatedElement]?.let {
            return it.size
        } ?: 0
    }

    fun getRequiredPages(elementCount: Int): Int {
        val pages = elementCount.toDouble() / guiList.count {
            it == paginatedElement
        }
        return ceil(pages).toInt()
    }

    fun getPaginatedElementsPerPage(elements: Map<String, List<GuiElementBuilder>>): Int {
        return guiList.count {
            it == paginatedElement
        }
    }

    fun getPage(requestedPage: Int, elementCount: Int): Int {
        val requiredPages = getRequiredPages(elementCount)
        var realPage = requestedPage
        if (requestedPage > requiredPages) {
            realPage = 1
        }
        if (requestedPage < 1) {
            realPage = requiredPages
        }
        return realPage
    }

    fun getGui(player: ServerPlayerEntity,
               elements: Map<String, List<GuiElementBuilder>>,
               onClose: (ServerPlayerEntity) -> Unit = {},
               requestedPage: Int = 1): SimpleGui {
         val gui = object : SimpleGui(GuiType.getScreenHandlerType(guiType), player, false) {
            override fun onClose() {
                onClose(player)
            }
        }

        gui.title = Rib.Rib.parseText(title)

        val paginatedElementCount = getPaginatedElementCount(elements)
        val page = getPage(requestedPage, paginatedElementCount)

        val elementIndex: MutableMap<String, Int> = mutableMapOf();

        elements.keys.forEach {
            if (paginated && paginatedElement == it) {
                val paginatedIndex = getPaginatedElementsPerPage(elements) * (page - 1)
                elementIndex[it] = paginatedIndex
            } else {
                elementIndex[it] = 0
            }
        }
        keys.keys.forEach {
            if (paginated && paginatedElement == it) {
                val paginatedIndex = getPaginatedElementsPerPage(elements) * (page - 1)
                elementIndex[it] = paginatedIndex
            } else {
                elementIndex[it] = 0
            }
        }

        for ((index, key) in guiList.withIndex()) {
            when (key) {
                forwardKey -> {
                    gui.setSlot(index,
                        getGuiElementFromKey(forwardKey)
                            .setCallback { _, _, _ ->
                                getGui(player, elements, onClose, page+1).open()
                            }
                        )
                }
                backKey -> {
                    gui.setSlot(index,
                        getGuiElementFromKey(forwardKey)
                            .setCallback { _, _, _ ->
                                getGui(player, elements, onClose, page-1).open()
                            }
                        )
                }
                else -> {
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
            }
        }

        return gui;
    }

}