package dev.roanoke.rib.utils

import dev.roanoke.rib.Rib
import net.minecraft.text.Text

class LoreLike(
    val lore: List<Text> = listOf()
) {

    companion object {
        fun ofString(string: String): LoreLike {
            return LoreLike(listOf((Rib.Rib.parseText(string))))
        }

        fun ofStringList(stringList: List<String>): LoreLike {
            return LoreLike(
                stringList.map {
                    Rib.Rib.parseText(it)
                }
            )
        }

        fun ofText(text: Text): LoreLike {
            return LoreLike(listOf(text))
        }
    }

}