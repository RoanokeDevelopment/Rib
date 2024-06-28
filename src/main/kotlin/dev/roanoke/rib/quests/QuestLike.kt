package dev.roanoke.rib.quests

import dev.roanoke.rib.utils.LoreLike
import net.minecraft.text.Text

interface QuestLike {

    fun isActive(): Boolean

    fun completed(): Boolean

    fun description(): LoreLike

    fun progress(): LoreLike

    fun taskAndProgress(): Text

}