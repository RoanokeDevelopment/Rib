package dev.roanoke.rib.quests

interface QuestProvider {

    fun getAllQuests(): List<Quest>
    fun saveQuests()

    fun getQuest(): Quest

    fun questAvailable(): Boolean

    fun onQuestProgress(quest: Quest)

    fun onQuestComplete(quest: Quest)

    fun isQuestActive(quest: Quest): Boolean

}