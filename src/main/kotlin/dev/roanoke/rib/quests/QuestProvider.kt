package dev.roanoke.rib.quests

interface QuestProvider {

    fun getAllQuests(): List<QuestLike>
    fun saveQuests()

    fun getQuest(): QuestLike

    fun questAvailable(): Boolean

    fun onQuestProgress(quest: QuestLike)

    fun onQuestComplete(quest: QuestLike)

    fun isQuestActive(quest: QuestLike): Boolean

}