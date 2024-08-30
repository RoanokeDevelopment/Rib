package dev.roanoke.rib.cobblemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.item.PokemonItem
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.PokemonStats
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.lang
import dev.roanoke.rib.Rib
import dev.roanoke.rib.Rib.Rib.parseText
import dev.roanoke.rib.utils.ItemBuilder
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun Stat.getCleanIdentifier(): String {
    return identifier.toString().substringAfter("${Cobblemon.MODID}:")
}

fun PokemonStats.matchesMap(map: Map<String, Int>): Boolean {

    Stats.PERMANENT.forEach { stat ->
        val value = getOrDefault(stat)

        map[stat.getCleanIdentifier()]?.let { checkValue ->
            if (value != checkValue) {
                return false
            }
        }
    }

    return true

}

fun Pokemon.getIVsLoreList(): String {
    val colours = listOf("<aqua>", "<light_purple>", "<aqua>", "<light_purple>", "<aqua>", "<light_purple>").iterator()
    return ivs.joinToString(" <reset>/ ") { (stat, value) ->
        "${if (colours.hasNext()) colours.next() else ""}${value}"
    }
}

fun Pokemon.getEVsLoreList(): String {
    val colours = listOf("<light_purple>", "<aqua>", "<light_purple>", "<aqua>", "<light_purple>", "<aqua>").iterator()
    return evs.joinToString(" <reset>/ ") { (stat, value) ->
        "${if (colours.hasNext()) colours.next() else ""}${value}"
    }
}

fun Pokemon.getMovesLoreList(accents: Iterator<String> = listOf("<aqua>", "<light_purple>", "<aqua>", "<light_purple>").iterator()): List<Text> {
    return moveSet.map {
        "- ${if (accents.hasNext()) accents.next() else ""}${it.displayName.string}"
    }.map { Rib.Rib.parseText(it) }
}

fun Pokemon.getGuiElementLore(): List<Text> {
    return listOf(
            "<dark_aqua>Item<reset>: ${if (heldItem().isOf(Items.AIR)) "None" else heldItem().name.string}",
            "<light_purple>Nature<reset>: ${nature.displayName.asTranslated().string}",
            "<dark_aqua>Ability<reset>: ${ability.displayName.asTranslated().string}",
            "<light_purple>IVs<reset>: ${getIVsLoreList()}",
            "<dark_aqua>EVs<reset>: ${getEVsLoreList()}",
            "<light_purple>Moves<reset:",
        ).map { parseText(it) } +
            getMovesLoreList()
}

fun Pokemon.getGenderIndicator(colour: Boolean = false): String {
    return when (gender) {
        Gender.GENDERLESS -> "⚲"
        Gender.FEMALE -> "${if (colour) "<light_purple>" else ""}♀"
        Gender.MALE -> "${if (colour) "<blue>" else ""}♂"
    }
}

fun Pokemon.getGuiElement(): GuiElementBuilder {
    return ItemBuilder(PokemonItem.from(this, 1))
        .hideAdditional()
        .setCustomName(Rib.Rib.parseText(
            "<light_purple>${getDisplayName().string}<reset> ${getGenderIndicator(colour=true)} <reset>(lvl ${level})"
        ))
        .addLore(getGuiElementLore())
        .gbuild()
}