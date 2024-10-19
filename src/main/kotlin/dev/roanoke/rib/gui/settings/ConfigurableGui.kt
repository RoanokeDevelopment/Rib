package dev.roanoke.rib.gui.settings

import net.minecraft.server.network.ServerPlayerEntity

abstract class ConfigurableGui {
    abstract fun save()
    fun openMenu(player: ServerPlayerEntity) {
        openMenu(player, {})
    }
    abstract fun openMenu(player: ServerPlayerEntity, onClose: (ServerPlayerEntity) -> Unit = {})
}