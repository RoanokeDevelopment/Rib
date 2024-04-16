package dev.roanoke.rib.quests

import dev.roanoke.rib.utils.LoreLike

interface QuestLike {

    fun isActive(): Boolean

    fun completed(): Boolean

    fun description(): LoreLike

    fun progress(): LoreLike

}