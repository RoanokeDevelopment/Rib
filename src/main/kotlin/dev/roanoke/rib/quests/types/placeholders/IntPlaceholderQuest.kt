package dev.roanoke.rib.quests.types.placeholders

import com.google.gson.JsonObject
import dev.roanoke.rib.Rib
import dev.roanoke.rib.quests.Quest
import dev.roanoke.rib.quests.QuestGroup
import dev.roanoke.rib.quests.QuestProvider
import dev.roanoke.rib.rewards.RewardList
import dev.roanoke.rib.utils.ItemBuilder
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.*
import kotlin.math.max

class IntPlaceholderQuest(name: String = "Default IntPlaceholder Quest Title",
                     id: String = UUID.randomUUID().toString(),
                     provider: QuestProvider,
                     group: QuestGroup,
                     var item: ItemBuilder,
                     var taskMessageString: String,
                     var placeholder: String,
                     var amount: Int = 6
) :
    Quest(name, id, provider, group) {

    companion object : Quest.QuestFactory {
        override fun fromState(json: JsonObject, state: JsonObject, provider: QuestProvider, group: QuestGroup): Quest {
            val name = json.get("name")?.asString ?: "Default IntPlaceholder Quest Title"
            val id = json.get("id")?.asString ?: UUID.randomUUID().toString()

            val placeholder = json.get("placeholder").asString

            val amount = json.get("amount").asInt
            val item = ItemBuilder.fromJson(json.get("item").asJsonObject)
            val taskMessage = json.get("taskMessage").asString

            val rRewards = RewardList.fromJson(json.get("rewards"))

            val rRewardsClaimed = state.get("rewardsClaimed")?.asBoolean ?: false

            return IntPlaceholderQuest(name, id, provider, group, item, taskMessage, placeholder, amount).apply {
                rewards = rRewards;
                rewardsClaimed = rRewardsClaimed
            }
        }
    }

    override fun getState(): JsonObject {
        return JsonObject().apply {
            addProperty("rewardsClaimed", rewardsClaimed)
        }
    }

    override fun applyState(state: JsonObject) {
        rewardsClaimed = state.get("rewardsClaimed")?.asBoolean ?: rewardsClaimed
    }

    override fun getButton(player: ServerPlayerEntity): GuiElementBuilder {
        return GuiElementBuilder.from(
            item
                .setCustomName(Rib.Rib.parseText(name))
                .addLore(getButtonLore()
            ).build()
        ).setCallback { _, _, _ ->
            getButtonCallback().invoke(player)
        }
    }

    fun getProgress(): Int {

        var progress = 0
        group.getOnlinePlayers().forEach {
            val placeholderResult = Placeholders.parseText(Text.literal(placeholder), PlaceholderContext.of(it))

            val inputString = placeholderResult.content.toString()
            val regex = "\\{(.*?)}".toRegex()
            val matchResult = regex.find(inputString)

            val valueInsideBraces = matchResult?.groups?.get(1)?.value

            Rib.LOGGER.info("Got Placeholder Result: ${placeholderResult.content.toString()}")
            progress = max(progress, valueInsideBraces?.toInt() ?: 0)
            Rib.LOGGER.info("Parsed, new progress is: $progress")
        }

        return progress

    }

    override fun completed(): Boolean {
        return getProgress() >= amount
    }

    override fun taskMessage(): Text {
        return Rib.Rib.parseText(taskMessageString)
    }

    override fun progressMessage(): Text {
        return Text.literal("(${getProgress()}/${amount})")
    }

}