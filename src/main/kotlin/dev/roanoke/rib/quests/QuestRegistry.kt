package dev.roanoke.rib.quests

import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import dev.roanoke.rib.quests.types.cobblemon.*
import dev.roanoke.rib.quests.types.minecraft.BreakBlockQuest
import dev.roanoke.rib.quests.types.minecraft.CraftItemQuest
import dev.roanoke.rib.quests.types.other.HasPermissionQuest
import dev.roanoke.rib.quests.types.placeholders.IntPlaceholderQuest

object QuestRegistry {

    private val questFactories: MutableMap<String, QuestFactory> = mutableMapOf()

    fun registerQuestType(type: String, factory: QuestFactory) {
        if (questFactories.containsKey(type)) {
            throw IllegalArgumentException("Quest type $type is already registered.")
        }
        Rib.LOGGER.info("[Rib] Registered Quest: $type")
        questFactories[type] = factory
    }

    fun createQuest(definition: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {
        val type = definition.get("type").asString

        val factory = questFactories[type] ?: throw IllegalArgumentException("Unsupported Quest Type: $type")
        return factory.fromJson(definition, state, provider, group)
    }

    fun registerDefaultQuests() {
        QuestRegistry.registerQuestType("BreakBlockQuest", BreakBlockQuest.Companion)
        QuestRegistry.registerQuestType("CraftItemQuest", CraftItemQuest.Companion)

        QuestRegistry.registerQuestType("CatchPokemonQuest", CatchPokemonQuest.Companion)
        QuestRegistry.registerQuestType("HarvestApricornQuest", HarvestApricornQuest.Companion)
        QuestRegistry.registerQuestType("DefeatPokemonQuest", DefeatPokemonQuest.Companion)
        QuestRegistry.registerQuestType("NicknamePokemonQuest", NicknamePokemonQuest.Companion)
        QuestRegistry.registerQuestType("TradePokemonQuest", TradePokemonQuest.Companion)
        QuestRegistry.registerQuestType("ReleasePokemonQuest", ReleasePokemonQuest.Companion)
        QuestRegistry.registerQuestType("EvolvePokemonQuest", EvolvePokemonQuest.Companion)
        QuestRegistry.registerQuestType("ReviveFossilQuest", ReviveFossilQuest.Companion)

        QuestRegistry.registerQuestType("IntPlaceholderQuest", IntPlaceholderQuest.Companion)
        QuestRegistry.registerQuestType("HasPermissionQuest", HasPermissionQuest.Companion)
    }

}
