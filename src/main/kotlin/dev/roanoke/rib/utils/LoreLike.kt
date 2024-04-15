package dev.roanoke.rib.utils

import dev.roanoke.rib.Rib
import net.minecraft.text.Text

class LoreLike {

    var lore: MutableList<Text> = mutableListOf()

    fun ofString(string: String) {
        lore.add(Rib.Rib.parseText(string))
    }

    fun ofStringList(stringList: List<String>) {
        lore.addAll(
            stringList.map {
                Rib.Rib.parseText(it)
            }
        )
    }

    fun ofText(text: Text) {
        lore.add(text)
    }

    fun ofTextList(textList: List<Text>) {
        lore.addAll(textList)
    }

}