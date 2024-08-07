package dev.roanoke.rib.utils

import com.google.gson.JsonParser
import dev.roanoke.rib.Rib
import net.minecraft.item.ItemStack
import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.nio.file.Path

class ItemManager(
    var itemsConfigPath: Path,
    var defaultItemsConfigPath: String
) {

    private var items: MutableMap<String, ItemBuilder> = mutableMapOf()

    fun reload() {
        items.clear()
        setup()
    }

    fun setup() {
        val file_exists = !itemsConfigPath.toFile().createNewFile()
        if (file_exists) {
            loadItems(itemsConfigPath.toFile())
        } else {
            importItemsFromJar(itemsConfigPath.toFile())
            loadItems(itemsConfigPath.toFile())
        }
    }

    fun importItemsFromJar(file: File) {
        val inputStream: InputStream = this::class.java.getResourceAsStream(defaultItemsConfigPath)
            ?: throw RuntimeException("Failed to find $defaultItemsConfigPath inside the jar.")

        try {
            inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            Rib.LOGGER.error("Failed to copy items.json from jar to config folder: ", e)
        }
    }


    fun loadItems(file: File) {
        val jsonObject = JsonParser.parseReader(FileReader(file)).asJsonObject

        jsonObject.entrySet().forEach { entry ->
            val itemName = entry.key
            val itemData = entry.value.asJsonObject

            val itemBuilder = ItemBuilder.fromJson(itemData)

            if (itemBuilder == null) {
                Rib.LOGGER.info("Failed to load Item from Config: $itemName")
            } else {
                Rib.LOGGER.info("Loaded Item from Config: ${itemName}")
                items[itemName] = itemBuilder
            }

        }
    }

    fun getItemStack(name: String): ItemStack {
        return items.getOrDefault(name, ItemBuilder("minecraft:gold_block")).build()
    }

    fun getItemBuilder(name: String): ItemBuilder {
        return items.getOrDefault(name, ItemBuilder("minecraft:gold_block"))
    }

}