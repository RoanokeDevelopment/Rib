package dev.roanoke.rib.gui

import net.minecraft.server.network.ServerPlayerEntity

interface GuiElement {

    fun openGui(player: ServerPlayerEntity)

}