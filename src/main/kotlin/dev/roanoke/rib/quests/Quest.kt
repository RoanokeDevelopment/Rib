package dev.roanoke.rib.quests

import com.google.gson.JsonObject
import dev.roanoke.rib.quests.types.BreakBlockQuest
import dev.roanoke.rib.quests.types.CatchPokemonQuest
import dev.roanoke.rib.quests.types.CraftItemQuest
import dev.roanoke.rib.quests.types.HarvestApricornQuest
import net.minecraft.text.Text
import java.util.UUID

abstract class Quest(
    var name: String = "Default Quest Title",
    var id: UUID = UUID.randomUUID(),
    var provider: QuestProvider,
    var group: QuestGroup
) {

    protected fun notifyProgress() {
        provider.onQuestProgress(this)
    }

    protected fun notifyCompletion() {
        if (completed()) {
            provider.onQuestComplete(this)
        }
    }

    fun isActive(): Boolean {
        return (!completed() && provider.isQuestActive(this))
    }

    abstract fun completed(): Boolean

    abstract fun taskMessage(): Text

    abstract fun progressMessage(): Text

    abstract fun saveState(): JsonObject

    interface QuestFactory {
        fun fromState(json: JsonObject, provider: QuestProvider, group: QuestGroup): Quest

    }

    companion object : QuestFactory {
        override fun fromState(json: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {
            throw UnsupportedOperationException("Use fromJson method instead.")
        }

        fun fromJson(json: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {
            val type = json.get("type").asString
            return when (type) {
                "BreakBlockQuest" -> BreakBlockQuest.fromState(json, provider, group)
                "CraftItemQuest" -> CraftItemQuest.fromState(json, provider, group)
                "CatchPokemonQuest" -> CatchPokemonQuest.fromState(json, provider, group)
                "HarvestApricornQuest" -> HarvestApricornQuest.fromState(json, provider, group)
                else -> throw IllegalArgumentException("Unsupported quest type: $type")
            }
        }
    }

}