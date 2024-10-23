package dev.roanoke.rib.gui.settings.types

import dev.roanoke.rib.Rib
import dev.roanoke.rib.gui.settings.BaseSetting
import dev.roanoke.rib.gui.settings.EasyConfigGui
import dev.roanoke.rib.gui.settings.SettingsManager
import dev.roanoke.rib.rewards.Reward
import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class RewardsSetting(
    override val name: String,
    private val getter: () -> MutableList<Reward>,
    private val setter: (MutableList<Reward>) -> Unit
): BaseSetting<MutableList<Reward>>() {

    override var settingsManager: SettingsManager? = null


    override var description: String = ""

    override fun getValue(): MutableList<Reward> = getter()

    override fun setValue(value: MutableList<Reward>) {
        setter(value)
    }

    override fun createGuiElement(player: ServerPlayerEntity): GuiElementBuilder {
        return guiElement
            .setName(Rib.Rib.parseText(name))
            .setLore(getLore())
            .setCallback { _, _, _ ->
                openManageRewards(player)
            }
    }

    fun openEditRewards(player: ServerPlayerEntity, reward: Reward) {

        val cGui = Rib.GUIs.getGui("generic_manage") ?: return

        val settings = SettingsManager(
            EasyConfigGui(
                save = { settingsManager?.save() },
                openMenu = { p -> openManageRewards(p) }
            )
        )

        settings.addSettings(
            StringSetting("Type", { reward.type }, { reward.type = it }),
            StringSetting("Value", { reward.value }, { reward.value = it }),
            StringSetting("Display", { reward.display}, { reward.display = it })
        )

        val gui = cGui.getGui(
            player = player,
            elements = mapOf(
                "X" to settings.getGuiElements(player)
            ),
            onClose = { p ->
                openManageRewards(p)
            }
        )

        gui.open()

    }

    private fun openManageRewards(player: ServerPlayerEntity) {

        val cGui = Rib.GUIs.getGui("generic_manage") ?: return

        val gui = cGui.getGui(
            player = player,
            elements = mapOf(
                "B" to listOf(
                    GuiElementBuilder(Items.ANVIL)
                        .setName(Rib.Rib.parseText("<green>Add Reward"))
                        .setCallback { _, _, _ ->
                            val rewards = getValue()
                            rewards.add(Reward())
                            setValue(rewards)
                            settingsManager?.save()
                            openManageRewards(player)
                        }
                ),
                "X" to getValue().map {
                    it.getGuiElement(post = listOf(
                        "",
                        "<green>Left Click<reset> to edit Reward",
                        "<red>Right Click<reset> to delete Reward",
                        ""
                    ).map { Rib.Rib.parseText(it) })
                        .setCallback { _, y: ClickType, _ ->
                            if (y.isRight) {
                                val newRewards = getValue()
                                newRewards.remove(it)
                                setValue(newRewards)
                                settingsManager?.save()
                                openManageRewards(player)
                            }
                            if (y.isLeft) {
                                openEditRewards(player, it)
                            }
                        }
                }
            ),
            onClose = { p ->
                settingsManager?.openMenu(p)
            }
        )

        gui.title = Rib.Rib.parseText("Manage Rewards: $name")

        gui.open()

    }

    override fun getLore(): List<Text> {
        val lore: MutableList<Text> = mutableListOf()
        lore.addAll(
            getDescriptionLore()
        )
        lore.addAll(listOf(
            "Rewards: ${getValue().size}",
            ""
        ).map { Rib.Rib.parseText(it) })
        lore.add(
            Rib.Rib.parseText("<gray>Click to edit / add new Rewards!")
        )
        return lore
    }

}