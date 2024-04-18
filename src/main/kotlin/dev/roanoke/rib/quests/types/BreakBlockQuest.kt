package dev.roanoke.rib.quests.types

import com.google.gson.JsonObject
import dev.roanoke.rib.quests.Quest
import dev.roanoke.rib.quests.QuestGroup
import dev.roanoke.rib.quests.QuestProvider
import dev.roanoke.rib.utils.ItemBuilder
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.block.Block
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.*

class BreakBlockQuest(name: String = "Default Quest Title",
                      uuid: UUID = UUID.randomUUID(),
                      provider: QuestProvider,
                      group: QuestGroup,
                      var block: Block,
                      var amount: Int = 3,
                      var progress: Int = 0
    ) :
    Quest(name, uuid, provider, group) {

    companion object : Quest.QuestFactory {
        override fun fromState(json: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {
            val name = json.get("name").asString
            val uuid = UUID.fromString(json.get("uuid").asString)

            val blockString = json.get("block").asString
            val block: Block = Block.getBlockFromItem(Registries.ITEM.get(Identifier.tryParse(blockString)))

            val amount = json.get("amount").asInt
            val progress = json.get("progress").asInt

            return BreakBlockQuest(name, uuid, provider, group, block, amount, progress)
        }
    }

    override fun getButton(player: ServerPlayerEntity): GuiElementBuilder {
        return GuiElementBuilder.from(
            ItemBuilder(block.asItem())
                .addLore(listOf(
                    taskMessage(),
                    progressMessage()
                )
            ).build()
        )
    }

    init {
        PlayerBlockBreakEvents.AFTER.register { world, player, pos, state, entity ->

            if (!isActive()) { return@register }

            if (group.includesPlayer(player as ServerPlayerEntity)) {
                if (state.block.equals(block)) {
                    progress++
                    this.notifyProgress()

                    if (completed()) {
                        this.notifyCompletion()
                    }

                }
            }

        }
    }

    override fun completed(): Boolean {
        return (progress >= amount)
    }

    override fun taskMessage(): Text {
        return Text.literal("Mine ${amount}x ").append(block.name).append("s")
    }

    override fun progressMessage(): Text {
        return Text.literal("${progress} / ${amount}")
    }

    override fun saveState(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", "BreakBlockQuest")
        jsonObject.addProperty("name", name)
        jsonObject.addProperty("uuid", id.toString())
        jsonObject.addProperty("block", Registries.ITEM.getId(block.asItem()).toString())
        jsonObject.addProperty("amount", amount)
        jsonObject.addProperty("progress", progress)
        return jsonObject
    }


}