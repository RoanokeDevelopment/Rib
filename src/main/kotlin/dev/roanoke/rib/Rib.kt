package dev.roanoke.rib

import dev.roanoke.rib.callbacks.RegisterQuestCallback
import dev.roanoke.rib.callbacks.RegisterRequirementCallback
import dev.roanoke.rib.callbacks.RibInitCallback
import dev.roanoke.rib.callbacks.RibPreInitCallback
import dev.roanoke.rib.gui.configurable.CGuiManager
import dev.roanoke.rib.quests.QuestRegistry
import dev.roanoke.rib.requirements.RequirementRegistry
import dev.roanoke.rib.utils.*
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.minecraft.command.argument.EntityArgumentType.player
import net.minecraft.entity.passive.SheepEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class Rib : ModInitializer {

    companion object {

        val MAIN_DIR = FabricLoader.getInstance().configDir.resolve("Rib")
        val GUI_DIR = MAIN_DIR.resolve("GUI")
        val MENU_DIR = GUI_DIR.resolve("Menus")

        @JvmField
        var LOGGER: Logger = LoggerFactory.getLogger("Rib")
        @JvmField
        var adventure: FabricServerAudiences? = null
        @JvmField
        var luckperms: LuckPerms? = null
        @JvmField
        var perm: PermissionManager = PermissionManager()
        @JvmField
        var server: MinecraftServer? = null

        val GUIs: CGuiManager = CGuiManager(MENU_DIR, GUI_DIR.resolve("item_definitions.json"))

    }

    object Rib {
        var mm = MiniMessage.miniMessage()

        fun parseText(string: String): Text {
            if (adventure == null) { return Text.literal(string) }
            return adventure!!.toNative(
                mm.deserialize(string)
            )
        }
    }

    override fun onInitialize() {

        FileUtils.copyResourceToFile("/rib/gui/menu/generic_manage.json", MENU_DIR.resolve("generic_manage.json"))
        FileUtils.copyResourceToFile("/rib/gui/menu/generic_paginated.json", MENU_DIR.resolve("generic_paginated.json"))
        FileUtils.copyResourceToFile("/rib/gui/item_definitions.json", GUI_DIR.resolve("item_definitions.json"))

        ServerLifecycleEvents.SERVER_STARTED.register {
            LOGGER.info("[RIB] Initialising Server, Adventure, LuckPerms")
            server = it
            adventure = FabricServerAudiences.of(it)
            try {
                luckperms = LuckPermsProvider.get()
            } catch (e: IllegalStateException) {
                LOGGER.error("LuckPerms is not installed or is broken!", e)
            }

            QuestRegistry.registerDefaultQuests()
            RegisterQuestCallback.EVENT.invoker().interact()

            RequirementRegistry.registerDefaults()
            RegisterRequirementCallback.EVENT.invoker().interact()

            RibPreInitCallback.EVENT.invoker().interact() // for crib to hook into location stuff etc

            GUIs.setup()
            RibInitCallback.EVENT.invoker().interact()

        }

    }

}
