package dev.roanoke.rib.quests

import dev.roanoke.rib.Rib
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.time.Duration
import java.util.UUID

class PlayerQuestGroup(
    var players: List<UUID>
): QuestGroup {

    companion object {

        fun fromPlayer(player: ServerPlayerEntity): PlayerQuestGroup {
            return PlayerQuestGroup(listOf(player.uuid))
        }

    }

    override fun sendMessage(message: Text) {
        Rib.server?.playerManager?.playerList?.forEach {
            if (players.contains(it.uuid)) {
                it.sendMessage(message)
            }
        }
    }

    override fun sendActionBar(message: Text) {
        Rib.server?.playerManager?.playerList?.forEach {
            if (players.contains(it.uuid)) {
                it.sendActionBar(message)
            }
        }
    }

    override fun includesPlayer(player: ServerPlayerEntity): Boolean {
        return players.contains(player.uuid)
    }

    override fun getOnlinePlayers(): List<ServerPlayerEntity> {
        return Rib.server?.playerManager?.playerList?.filter {
            players.contains(it.uuid)
        } ?: listOf()
    }

    override fun sendTitle(title: String, subtitle: String, fadeIn: Long, stayTime: Long, fadeOut: Long) {
        Rib.server?.let { server ->

            val times: Title.Times = Title.Times.times(Duration.ofMillis(fadeIn), Duration.ofMillis(stayTime), Duration.ofMillis(fadeOut))
            val titleComponent: Title = Title.title(Component.text(title), Component.text(subtitle), times)

            server.playerManager.playerList.forEach {
                if (includesPlayer(it)) {
                    it.showTitle(titleComponent)
                }
            }

        }
    }

}