package dev.roanoke.rib.utils

import dev.roanoke.rib.Rib
import net.minecraft.text.Text

class LoreLike {

    var lore: MutableList<Text> = mutableListOf()

    fun of(string: String) {
        lore.add(Rib.Rib.parseText(string))
    }

    fun of(stringList: List<String>) {
        lore.addAll(
            stringList.map {
                Rib.Rib.parseText(it)
            }
        )
    }

    fun of(text: Text) {
        lore.add(text)
    }

    fun of(textList: List<Text>) {
        lore.addAll(textList)
    }

}