package dev.roanoke.rib.gui.configurable

import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import dev.roanoke.rib.utils.ItemBuilder
import dev.roanoke.rib.utils.ItemManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.nio.file.Files
import java.nio.file.Path

class CGuiManager(private val guiDirectory: Path, private val itemDefinitions: Path) {

    val items: MutableMap<String, ItemBuilder> = mutableMapOf()
    val guis: MutableList<ConfiguredGUI> = mutableListOf()

    init {
        loadItems()
        loadGUIs()
    }

    private fun loadItems() {
        try {
            val jsonContent = Files.readString(itemDefinitions)
            val jsonObject = Gson().fromJson(jsonContent, JsonObject::class.java)

            for ((key, value) in jsonObject.entrySet()) {
                if (value.isJsonObject) {
                    val itemObject = value.asJsonObject
                    val item = ItemBuilder.fromJson(itemObject)
                    item?.let {
                        items[key] = it
                    }
                }
            }
        } catch (e: Exception) {
            Rib.LOGGER.error("Error loading Items in GUI Manager. ", e)
        }
    }

    private fun loadGUIs() {
        try {
            Files.newDirectoryStream(guiDirectory, "*.json").use { directoryStream ->
                for (path in directoryStream) {
                    val jsonContent = Files.readString(path)
                    val jsonObject = Json.parseToJsonElement(jsonContent).jsonObject
                    val cGui = ConfiguredGUI.fromJson(jsonObject, items)
                    cGui?.let {
                        guis.add(cGui)
                    }
                }
            }
        } catch (e: Exception) {
            Rib.LOGGER.error("Error loading GUIs in GUI Manager. ", e)
        }
    }

}