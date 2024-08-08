package dev.roanoke.rib.cobblemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.pokemon.PokemonStats
import dev.roanoke.rib.Rib

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