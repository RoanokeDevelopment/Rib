package dev.roanoke.rib.quests

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

interface QuestGroup {

    fun sendMessage(message: Text)

    fun sendActionBar(message: Text)

    fun sendTitle(title: String, subtitle: String, fadeIn: Long = 500, stayTime: Long = 3000, fadeOut: Long = 1000)

    fun includesPlayer(player: ServerPlayerEntity): Boolean

    fun getOnlinePlayers(): List<ServerPlayerEntity>


}