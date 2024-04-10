package dev.roanoke.rib.rewards

import com.google.gson.JsonObject
import net.minecraft.item.Items
import net.minecraft.network.message.SignedMessage
import net.minecraft.server.network.ServerPlayerEntity
import dev.roanoke.rib.Rib
import dev.roanoke.rib.gui.Gym.GymMenus
import dev.roanoke.rib.gui.Gym.GymMenus.Companion.openPlayerRewardsGUI
import dev.roanoke.rib.gui.Manager.ManageMenus
import dev.roanoke.rib.gui.SidePage
import dev.roanoke.rib.Gyms.Gym
import dev.roanoke.rib.Gyms.GymResult
import dev.roanoke.rib.interfaces.FieldChangeRequest
import dev.roanoke.rib.interfaces.FieldChangeType

class GymReward (
    var result: GymResult = GymResult.GYM_DEFEATED,
    var type: String = "none",
    var value: String = "",
    var gymName: String = ""
) : FieldChangeRequest {

    constructor(json: JsonObject, gymName: String) : this() {
        if (json.has("type")) {
            type = json.get("type").asString
        }
        if (json.has("result")) {
            result = try {
                GymResult.valueOf(json.get("result").asString.uppercase())
            } catch (e: Exception) {
                GymResult.GYM_DEFEATED
            }
        }
        if (json.has("value")) {
            value = json.get("value").asString
        }
        this.gymName = gymName
    }

    fun getGym(): Gym? {
        return Rib.gymManager.getGym(gymName)
    }

    fun isValid(): Boolean {
        if (RewardsManager.rewardTypes.contains(type) && value != "") {
            return true
        }
        return false
    }

    fun getRewardCommand(placeholders: MutableMap<String, String>): String {
        var command: String = value;
        placeholders.forEach {
            command = command.replace(it.key, it.value)
        }
        return command
    }

    // ONLY to be used for GUI examples
    private fun getRewardCommand(player: ServerPlayerEntity): String {
        var placeholders: MutableMap<String, String> = mutableMapOf(
            "{challenger}" to player.gameProfile.name,
            "{player}" to player.gameProfile.name,
            "{leader}" to player.gameProfile.name
        )
        return getRewardCommand(placeholders)
    }

    // placeholders: {challenger}, {player} = challenger, {leader} = gym leader
    fun executeReward(placeholders: MutableMap<String, String>) {
        if (type == "command") {
            Rib.server!!.commandManager.dispatcher.execute(
                getRewardCommand(placeholders),
                Rib.server!!.commandSource
            )
        }
    }

    fun getPlaceholders(): Map<String, String> {
        return mapOf(
            "{reward_value}" to this.value,
            "{reward_type}" to this.type,
            "{battle_result}" to this.result.prettyString()
        )
    }

    override fun applyChange(fieldType: FieldChangeType, message: SignedMessage, sender: ServerPlayerEntity) {
        when (fieldType) {
            FieldChangeType.VALUE -> {
                this.value = message.content.string
                sender.sendMessage(Rib.messages.getDisplayMessage("ggyms.editing.change_reward_value_confirm", getPlaceholders()))
                openRewardView(sender)
            }
            else -> {
                sender.sendMessage(Rib.messages.getDisplayMessage("ggyms.error.invalid_change_request"))
            }
        }
    }

    fun openRewardView(player: ServerPlayerEntity) {
        val rewardGUI: SidePage = SidePage(
            player,
            title = "Viewing Reward (${result.prettyString()})",
            buttons = mutableListOf(
                GymMenus.getBackButton("Back to Gym Menu") {
                    if (this.getGym() != null) {
                        GymMenus.openPlayerRewardsGUI(player, getGym()!!)
                    } else {
                        ManageMenus.getManageGymsGui(player).open()
                    }
                },
                GymMenus.getInfoElement(Items.GOLD_ORE, "<white>Reward Info", mutableListOf(
                    "<white>Event: <gray>${this.result.prettyString()}",
                    "<white>Type: <gray>${this.type}",
                    "<white>Value: <gray>${this.value}",
                    "<white>Result: <gray>${this.getRewardCommand(player)}"
                )),
                GymMenus.getCallbackElement(Items.GOLD_BLOCK, "Saving & Deleting Options", mutableListOf(
                    "<green>Left Click <white>to Save this Gym (& Reward)",
                    "<red>Right Click <white>to Delete this Reward"
                )) { x, y, z ->
                    val gym: Gym? = this.getGym()
                    if (gym == null) {
                        player.sendMessage(Rib.messages.getDisplayMessage("ggyms.error.reward_has_no_gym"))
                    } else {
                        if (y.isLeft) {
                            Rib.gymManager.saveGymToFile(gym)
                            player.sendMessage(
                                Rib.messages.getDisplayMessage(
                                    "ggyms.action.save_gym",
                                    gym.getPlaceholders()
                                )
                            )
                        } else if (y.isRight) {
                            gym.removeReward(this)
                            player.sendMessage(
                                Rib.messages.getDisplayMessage(
                                    "ggyms.action.remove_reward",
                                    gym.getPlaceholders()
                                )
                            )
                            openPlayerRewardsGUI(player, gym)
                        }
                    }
                }
            ),
            elements = mutableListOf(
                GymMenus.getInfoElement(Items.NAME_TAG, "Change Reward Type", mutableListOf(
                    "<gray>Currently, the only reward type is 'command'",
                    "<gray>(which means you can't change this)"
                )),
                GymMenus.getCallbackElement(Items.NAME_TAG, "Change Reward Value", mutableListOf(
                    "<gray>Currently: <dark_aqua>${this.value}",
                    "<gray>For a 'command' reward, this is the command that'll get executed.",
                    "<gray>You can use the placeholders {player} and {leader} to get challenger/gym leader",
                    "<gray>Remember to set the event! Rewards can be executed when the Gym wins or loses."
                )) { x, y, z ->
                    player.sendMessage(Rib.messages.getDisplayMessage("ggyms.editing.change_reward_value_prompt"))
                    Rib.chatListeners.listenForChangeRequest(player, this, FieldChangeType.VALUE)
                    GymMenus.closeGui(player)
                },
                GymMenus.getCallbackElement(Items.NAME_TAG, "Change Reward Event", mutableListOf(
                    "<gray>This reward executes when the battle result is: <dark_aqua>${result.prettyString()}",
                    "<gray>You can execute rewards for these events; 'Gym Defeated', 'Player Defeated', 'Any Result'",
                    "<gray>",
                    "<dark_aqua>Left click to cycle through Reward Event options"
                )) { x, y, z ->
                    if (y.isLeft) {
                        this.result = result.nextResult()
                        player.sendMessage(Rib.messages.getDisplayMessage("ggyms.action.changed_reward_event", getPlaceholders()))
                        this.openRewardView(player)
                    }
                }
            )
        )
        rewardGUI.open()
    }
}