package dev.roanoke.rib.quests.types.minecraft

import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import dev.roanoke.rib.quests.Quest
import dev.roanoke.rib.quests.QuestFactory
import dev.roanoke.rib.quests.QuestGroup
import dev.roanoke.rib.quests.QuestProvider
import dev.roanoke.rib.rewards.RewardList
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
                      id: String = UUID.randomUUID().toString(),
                      provider: QuestProvider,
                      group: QuestGroup,
                      var block: Block,
                      var amount: Int = 3,
                      var progress: Int = 0,
    ) :
    Quest(name, id, provider, group) {

    companion object : QuestFactory {
        override fun fromJson(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {

            val blockString = json.get("block")?.asString ?: "minecraft:stone"
            val block: Block = Block.getBlockFromItem(Registries.ITEM.get(Identifier.tryParse(blockString)))

            val amount = json.get("amount")?.asInt ?: 3

            val progress = state.get("progress")?.asInt ?: 0

            return BreakBlockQuest(
                provider = provider, group = group,
                block = block, amount = amount,
                progress = progress).apply {
                    loadDefaultValues(json, state)
            }
        }
    }

    override fun getState(): JsonObject {
        return JsonObject().apply {
            addProperty("progress", progress)
            addProperty("rewardsClaimed", rewardsClaimed)
        }
    }

    override fun applyState(state: JsonObject) {
        progress = state.get("progress")?.asInt ?: progress
        rewardsClaimed = state.get("rewardsClaimed")?.asBoolean ?: rewardsClaimed
    }

    override fun getButton(player: ServerPlayerEntity): GuiElementBuilder {
        return GuiElementBuilder.from(
            ItemBuilder(block.asItem())
                .setCustomName(Rib.Rib.parseText(name))
                .addLore(
                    getButtonLore()
            ).build()
        ).setCallback { _, _, _ ->
            getButtonCallback().invoke(player)
        }
    }

    init {
        PlayerBlockBreakEvents.AFTER.register { world, player, pos, state, entity ->

            if (!isActive()) { return@register }

            if (group.includesPlayer(player as ServerPlayerEntity)) {
                if (state.block.equals(block)) {
                    progress++
                    this.notifyProgress()
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
        return Text.literal("(${progress}/${amount})")
    }

}