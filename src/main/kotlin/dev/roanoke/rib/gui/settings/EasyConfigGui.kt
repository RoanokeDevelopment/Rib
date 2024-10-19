package dev.roanoke.rib.gui.settings

import net.minecraft.server.network.ServerPlayerEntity

class EasyConfigGui(
    val save: () -> Unit,
    val openMenu: (ServerPlayerEntity) -> Unit
): ConfigurableGui() {

    override fun save() {
        save.invoke()
    }

    override fun openMenu(player: ServerPlayerEntity, onClose: (ServerPlayerEntity) -> Unit) {
        openMenu.invoke(player)
    }


}