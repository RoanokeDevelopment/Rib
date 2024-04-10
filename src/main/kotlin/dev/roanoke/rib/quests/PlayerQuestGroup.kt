package dev.roanoke.rib.quests

import net.minecraft.server.network.ServerPlayerEntity
import java.util.UUID

class PlayerQuestGroup(
    var players: List<UUID>
) {

    companion object {

        fun fromPlayer(player: ServerPlayerEntity): PlayerQuestGroup {
            return PlayerQuestGroup(listOf(player.uuid))
        }

    }

}