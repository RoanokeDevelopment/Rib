package dev.roanoke.rib.quests

import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import dev.roanoke.rib.gui.ButtonElement
import dev.roanoke.rib.quests.types.BreakBlockQuest
import dev.roanoke.rib.quests.types.CatchPokemonQuest
import dev.roanoke.rib.quests.types.CraftItemQuest
import dev.roanoke.rib.quests.types.HarvestApricornQuest
import dev.roanoke.rib.utils.ItemBuilder
import dev.roanoke.rib.utils.LoreLike
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.UUID

abstract class Quest(
    var name: String = "Default Quest Title",
    var id: String = UUID.randomUUID().toString(),
    var provider: QuestProvider,
    var group: QuestGroup
): QuestLike, ButtonElement {

    override fun getButton(player: ServerPlayerEntity): GuiElementBuilder {
        return GuiElementBuilder.from(
            ItemBuilder(Items.STONE)
                .setCustomName(Rib.Rib.parseText(name))
                .addLore(listOf(
                    taskMessage(),
                    progressMessage()
                )
            ).build()
        )
    }

    protected fun notifyProgress() {
        provider.onQuestProgress(this)
    }

    protected fun notifyCompletion() {
        if (completed()) {
            provider.onQuestComplete(this)
        }
    }

    override fun description(): LoreLike {
        return LoreLike.ofText(taskMessage())
    }

    override fun progress(): LoreLike {
        return LoreLike.ofText(progressMessage())
    }

    override fun isActive(): Boolean {
        return (!completed() && provider.isQuestActive(this))
    }

    abstract fun taskMessage(): Text

    abstract fun progressMessage(): Text

    abstract fun getState(): JsonObject

    @Deprecated(
        message = "The definition & state of a Quest should be stored separately. Use getState() to get relevant stateful information as a JsonObject",
        replaceWith = ReplaceWith("getState()")
    )
    abstract fun saveState(): JsonObject

    abstract fun applyState(state: JsonObject)

    interface QuestFactory {
        fun fromState(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest

    }

    companion object : QuestFactory {
        override fun fromState(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {
            throw UnsupportedOperationException("Use fromJson method instead.")
        }

        fun fromJson(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {
            val type = json.get("type").asString
            return when (type) {
                "BreakBlockQuest" -> BreakBlockQuest.fromState(json, state, provider, group)
                "CraftItemQuest" -> CraftItemQuest.fromState(json, state, provider, group)
                "CatchPokemonQuest" -> CatchPokemonQuest.fromState(json, state, provider, group)
                "HarvestApricornQuest" -> HarvestApricornQuest.fromState(json, state, provider, group)
                else -> throw IllegalArgumentException("Unsupported quest type: $type")
            }
        }
    }

}