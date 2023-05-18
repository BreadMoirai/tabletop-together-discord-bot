package com.github.breadmoirai.discordtabletop.core.games.frosthaven.building

import com.github.breadmoirai.discordtabletop.core.games.frosthaven.FHResource
import com.github.breadmoirai.discordtabletop.discord.bold

class FHBuildingDeck(val id: String, buildings: List<FHBuilding>) {
    val buildings: MutableList<FHBuilding> = buildings.toMutableList()

    fun wreck(building: FHBuilding) {
        assert(building.wrecked.not())
        assert(building in buildings)
        buildings.remove(building)
        buildings.add(building.copy(wrecked = true))
    }

    fun rebuild(building: FHBuilding) {
        assert(building.wrecked)
        assert(building in buildings)
        buildings.remove(building)
        buildings.add(building.copy(wrecked = true))
    }

    fun unlock(id: String): FHBuilding {
        return FHBuildings.getBuilding(id, 0).also {
            buildings.add(it)
        }
    }

    fun upgrade(building: FHBuilding): FHBuilding {
        buildings.remove(building)
        return FHBuildings.getBuilding(building.id, building.level+1).also {
            buildings.add(it)
        }
    }

    val formatted: String
        get() = buildString {
            append("Buildings: ".bold())
            val (unbuilt, built) = buildings.sortedWith(compareBy(
                { it.wrecked },
                { it.upgradeCost.isNotEmpty() },
                { it.upgradeCost.find { resource -> resource is FHResource.Prosperity }?.count },
                { it.upgradeCost.sumOf { resource -> resource.count } }
            )).partition { it.level == 0 }
            for (building in built) {
                append("\n\t")
                append(building.formatted)
            }
            append("\nAvailable Buildings")
            for (building in unbuilt) {
                append("\n\t")
                append(building.formatted)
            }
        }
}
