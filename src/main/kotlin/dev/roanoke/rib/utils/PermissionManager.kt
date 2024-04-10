package dev.roanoke.rib.utils

import net.luckperms.api.model.user.User
import net.minecraft.server.network.ServerPlayerEntity
import dev.roanoke.rib.Rib

class PermissionManager {

    fun getLuckPermsUser(player: ServerPlayerEntity): User {
        return Rib.luckperms!!.getPlayerAdapter(
            ServerPlayerEntity::class.java
        ).getUser(player)
    }

    fun hasPermission(player: ServerPlayerEntity, permission: String): Boolean {
        val user = getLuckPermsUser(player)
        val permissionData = user.cachedData.permissionData
        return permissionData.checkPermission(permission).asBoolean()
    }

}