package dev.roanoke.rib.rewards

import com.google.gson.JsonArray

class RewardsManager {
    companion object {
        var rewardTypes = listOf("command")

        fun getRewardsFromList(rewardList: JsonArray, gymName: String = ""): MutableList<GymReward> {
            val resultList = mutableListOf<GymReward>()

            // Iterate through each element in the JsonArray
            for (element in rewardList) {
                if (element.isJsonObject) {
                    // Construct the GymReward from the JsonObject
                    val reward = GymReward(element.asJsonObject, gymName)

                    // Check if the reward is valid
                    if (reward.isValid()) {
                        // If valid, add to the result list
                        resultList.add(reward)
                    }
                }
            }

            return resultList
        }

    }
}