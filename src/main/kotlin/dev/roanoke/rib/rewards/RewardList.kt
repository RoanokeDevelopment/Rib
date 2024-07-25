package dev.roanoke.rib.rewards

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import kotlinx.serialization.json.jsonObject
import net.minecraft.server.network.ServerPlayerEntity

class RewardList(
    val rewards: MutableList<Reward> = mutableListOf()
) {

    companion object {
        fun fromJson(rewardsElement: JsonElement?): RewardList {
            val rewardList = RewardList()
            rewardsElement?.asJsonArray?.forEach {
                rewardList.rewards.add(
                    Reward(it.asJsonObject)
                )
            }
            return rewardList
        }

    }

    fun toJson(): kotlinx.serialization.json.JsonArray {
        return kotlinx.serialization.json.JsonArray(rewards.map {
            it.toJson()
        })
    }

    fun executeRewards(player: ServerPlayerEntity) {
        rewards.forEach { reward ->
            try {
                reward.executeReward(player)
            } catch (e: Exception) {
                Rib.LOGGER.error("Error executing reward ${player.name}: ${e.message}")
            }
        }
    }

}