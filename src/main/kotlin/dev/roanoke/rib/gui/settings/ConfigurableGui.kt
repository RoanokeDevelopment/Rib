package dev.roanoke.rib.gui.settings

import net.minecraft.server.network.ServerPlayerEntity

interface ConfigurableGui {
    fun save()
    fun openMenu(player: ServerPlayerEntity)
}