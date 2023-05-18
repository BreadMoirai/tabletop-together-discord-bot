package com.github.breadmoirai.discordtabletop.core.games.frosthaven.building

import com.github.breadmoirai.discordtabletop.core.games.frosthaven.FHResource.*

object FHBuildings {

    fun startingDeck(id: String): FHBuildingDeck {
        return FHBuildingDeck(id, startingBuildings())
    }

    fun getBuilding(id: String, level: Int): FHBuilding {
        val find = BUILDINGS.find { it.id == id && it.level == level }
        assert(find != null) { "No such building with id $id and level $level" }
        return find!!
    }

    fun startingBuildings() = BUILDINGS.filter {
        (it.id in listOf("34", "35", "84", "98") && it.level == 1) ||
                (it.id in listOf("A", "B", "C", "J", "K", "L", "M", "N") && it.level == 0)
    }

    val BUILDINGS = listOf(
        FHBuilding(
             "5", "Mining Camp", 0,
            listOf(Prosperity(1), Lumber(4), Metal(2), Hide(1), Coin(10)),
            listOf()
        ),
        FHBuilding(
             "5", "Mining Camp", 1,
            listOf(Prosperity(3), Lumber(6), Metal(3), Hide(2)),
            listOf(Lumber(1), Metal(2))
        ),
        FHBuilding(
             "5", "Mining Camp", 2,
            listOf(Prosperity(5), Lumber(8), Metal(5), Hide(2)),
            listOf(Lumber(1), Metal(2), Hide(1))
        ),
        FHBuilding(
             "5", "Mining Camp", 3,
            listOf(Prosperity(7), Lumber(10), Metal(6), Hide(3)),
            listOf(Lumber(2), Metal(2), Hide(1))
        ),
        FHBuilding(
             "5", "Mining Camp", 4,
            listOf(),
            listOf(Lumber(2), Metal(2), Hide(1))
        ),
        FHBuilding(
             "12", "Hunting Lodge", 0,
            listOf(Prosperity(1), Lumber(4), Metal(1), Hide(2), Coin(10)),
            listOf()
        ),
        FHBuilding(
             "12", "Hunting Lodge", 1,
            listOf(Prosperity(3), Lumber(6), Metal(2), Hide(3)),
            listOf(Lumber(2), Metal(0), Hide(1))
        ),
        FHBuilding(
             "12", "Hunting Lodge", 2,
            listOf(Prosperity(5), Lumber(8), Metal(2), Hide(5)),
            listOf(Lumber(2), Metal(1), Hide(1))
        ),
        FHBuilding(
             "12", "Hunting Lodge", 3,
            listOf(Prosperity(7), Lumber(10), Metal(3), Hide(6)),
            listOf(Lumber(2), Metal(1), Hide(2))
        ),
        FHBuilding(
             "12", "Hunting Lodge", 4,
            listOf(),
            listOf(Lumber(2), Metal(1), Hide(2))
        ),
        FHBuilding(
             "17", "Logging Camp", 0,
            listOf(Prosperity(1), Lumber(2), Metal(3), Hide(2), Coin(10)),
            listOf()
        ),
        FHBuilding(
             "17", "Logging Camp", 1,
            listOf(Prosperity(3), Lumber(4), Metal(5), Hide(2)),
            listOf(Lumber(2), Metal(1))
        ),
        FHBuilding(
             "17", "Logging Camp", 2,
            listOf(Prosperity(5), Lumber(6), Metal(6), Hide(3)),
            listOf(Lumber(2), Metal(1), Hide(1))
        ),
        FHBuilding(
             "17", "Logging Camp", 3,
            listOf(Prosperity(7), Lumber(8), Metal(8), Hide(3)),
            listOf(Lumber(2), Metal(2), Hide(1))
        ),
        FHBuilding(
             "17", "Logging Camp", 4,
            listOf(),
            listOf(Lumber(2), Metal(2), Hide(1))
        ),
        FHBuilding(
             "21", "Inn", 0,
            listOf(Prosperity(2), Lumber(4), Metal(4), Hide(4), Coin(18)),
            listOf()
        ),
        FHBuilding(
             "21", "Inn", 1,
            listOf(Prosperity(4), Lumber(5), Metal(5), Hide(5)),
            listOf(Lumber(2), Metal(2), Hide(1))
        ),
        FHBuilding(
             "21", "Inn", 2,
            listOf(Prosperity(5), Lumber(6), Metal(6), Hide(6)),
            listOf(Lumber(2), Metal(2), Hide(2))
        ),
        FHBuilding(
             "21", "Inn", 3,
            listOf(),
            listOf(Lumber(3), Metal(2), Hide(2))
        ),
        FHBuilding(
             "24", "Garden", 0,
            listOf(Prosperity(1), Lumber(3), Coin(18)),
            listOf()
        ),
        FHBuilding(
             "24", "Garden", 1,
            listOf(Prosperity(2), Lumber(4), Metal(2), Hide(2)),
            listOf(Lumber(2))
        ),
        FHBuilding(
             "24", "Garden", 2,
            listOf(Prosperity(5), Lumber(3), Metal(3), Hide(3)),
            listOf(Lumber(2), Metal(1))
        ),
        FHBuilding(
             "24", "Garden", 3,
            listOf(Prosperity(7), Lumber(6), Metal(3), Hide(3), Morale(1)),
            listOf(Lumber(2), Metal(1), Hide(1))
        ),
        FHBuilding(
             "24", "Garden", 4,
            listOf(),
            listOf(Lumber(3), Metal(1), Hide(1))
        ),
        FHBuilding(
             "34", "Craftsman", 1,
            listOf(Prosperity(1), Lumber(2), Metal(2), Hide(1)),
            listOf(Lumber(1), Metal(1))
        ),
        FHBuilding(
             "34", "Craftsman", 2,
            listOf(Prosperity(2), Lumber(3), Metal(2), Hide(2)),
            listOf(Lumber(1), Metal(1), Hide(1))
        ),
        FHBuilding(
             "34", "Craftsman", 3,
            listOf(Prosperity(3), Lumber(4), Metal(3), Hide(2)),
            listOf(Lumber(2), Metal(1), Hide(1))
        ),
        FHBuilding(
             "34", "Craftsman", 4,
            listOf(Prosperity(4), Lumber(5), Metal(3), Hide(3)),
            listOf(Lumber(3), Metal(1), Hide(1))
        ),
        FHBuilding(
             "34", "Craftsman", 5,
            listOf(Prosperity(5), Lumber(6), Metal(4), Hide(3)),
            listOf(Lumber(3), Metal(1), Hide(1))
        ),
        FHBuilding(
             "34", "Craftsman", 6,
            listOf(Prosperity(6), Lumber(7), Metal(4), Hide(4)),
            listOf(Lumber(3), Metal(2), Hide(1))
        ),
        FHBuilding(
             "34", "Craftsman", 7,
            listOf(Prosperity(7), Lumber(8), Metal(5), Hide(4), Morale(1)),
            listOf(Lumber(3), Metal(2), Hide(1))
        ),
        FHBuilding(
             "34", "Craftsman", 8,
            listOf(Prosperity(8), Lumber(9), Metal(5), Hide(5), Morale(1)),
            listOf(Lumber(3), Metal(2), Hide(2))
        ),
        FHBuilding(
             "34", "Craftsman", 9,
            listOf(),
            listOf(Lumber(3), Metal(2), Hide(2))
        ),
        FHBuilding(
             "35", "Alchemist", 1,
            listOf(Prosperity(1), Lumber(2), Metal(2), Hide(1)),
            listOf(Lumber(1), Metal(1))
        ),
        FHBuilding(
             "35", "Alchemist", 2,
            listOf(Prosperity(4), Lumber(4), Metal(4), Hide(2)),
            listOf(Lumber(1), Metal(1), Hide(1))
        ),
        FHBuilding(
             "35", "Alchemist", 3,
            listOf(Prosperity(4), Lumber(4), Metal(4), Hide(2)),
            listOf(Lumber(3), Metal(1), Hide(1))
        ),
        FHBuilding(
             "37", "Trading Post", 0,
            listOf(Prosperity(2), Lumber(2), Metal(2), Hide(1), Coin(18)),
            listOf()
        ),
        FHBuilding(
             "37", "Trading Post", 1,
            listOf(Prosperity(3), Lumber(3), Metal(3), Hide(2)),
            listOf(Lumber(1), Metal(1))
        ),
        FHBuilding(
             "37", "Trading Post", 2,
            listOf(Prosperity(5), Lumber(4), Metal(3), Hide(3)),
            listOf(Lumber(1), Metal(1), Hide(1))
        ),
        FHBuilding(
             "37", "Trading Post", 3,
            listOf(Prosperity(7), Lumber(5), Metal(4), Hide(4), Morale(1)),
            listOf(Lumber(2), Metal(1), Hide(1))
        ),
        FHBuilding(
             "37", "Trading Post", 4,
            listOf(),
            listOf(Lumber(2), Metal(2), Hide(1))
        ),
        FHBuilding(
             "39", "Jeweler", 0,
            listOf(Prosperity(4), Lumber(3), Metal(2), Hide(4), Coin(18)),
            listOf()
        ),
        FHBuilding(
             "39", "Jeweler", 1,
            listOf(Prosperity(6), Lumber(6), Metal(3)),
            listOf(Lumber(1), Metal(2), Hide(1))
        ),
        FHBuilding(
             "39", "Jeweler", 2,
            listOf(Prosperity(8), Lumber(2), Metal(10), Hide(3), Morale(1)),
            listOf(Lumber(1), Metal(3), Hide(1))
        ),
        FHBuilding(
             "39", "Jeweler", 3,
            listOf(),
            listOf(Lumber(1), Metal(4), Hide(1))
        ),
        FHBuilding(
             "42", "Temple of the Great Oak", 0,
            listOf(Prosperity(1), Lumber(4), Metal(2), Hide(2), Coin(18)),
            listOf(Lumber(2), Metal(1), Hide(1))
        ),
        FHBuilding(
             "42", "Temple of the Great Oak", 1,
            listOf(Prosperity(4), Lumber(3), Metal(6), Hide(3)),
            listOf(Lumber(2), Metal(2), Hide(1))
        ),
        FHBuilding(
             "42", "Temple of the Great Oak", 2,
            listOf(Prosperity(7), Lumber(4), Metal(10), Hide(4), Morale(1)),
            listOf(Lumber(2), Metal(2), Hide(1))
        ),
        FHBuilding(
             "42", "Temple of the Great Oak", 3,
            listOf(),
            listOf(Lumber(3), Metal(2), Hide(1))
        ),
        FHBuilding(
             "44", "Enhancer", 0,
            listOf(Prosperity(1), Lumber(3), Metal(4), Coin(18)),
            listOf()
        ),
        FHBuilding(
             "44", "Enhancer", 1,
            listOf(Prosperity(3), Lumber(4), Metal(5)),
            listOf(Lumber(2), Metal(2))
        ),
        FHBuilding(
             "44", "Enhancer", 2,
            listOf(Prosperity(5), Lumber(4), Metal(4), Hide(4)),
            listOf(Lumber(3), Metal(2))
        ),
        FHBuilding(
             "44", "Enhancer", 3,
            listOf(Prosperity(7), Lumber(5), Metal(6), Hide(6), Morale(1)),
            listOf(Lumber(3), Metal(2), Hide(1))
        ),
        FHBuilding(
             "44", "Enhancer", 4,
            listOf(Prosperity(7), Lumber(5), Metal(6), Hide(6), Morale(1)),
            listOf(Lumber(3), Metal(2), Hide(2))
        ),
        FHBuilding(
             "65", "Metal Depot", 0,
            listOf(Prosperity(3), Lumber(2), Metal(6), Hide(2), Coin(18)),
            listOf()
        ),
        FHBuilding(
             "65", "Metal Depot", 1,
            listOf(Prosperity(7), Lumber(5), Metal(5), Hide(5)),
            listOf(Lumber(1), Metal(2), Hide(1))
        ),
        FHBuilding(
             "65", "Metal Depot", 2,
            listOf(),
            listOf(Lumber(1), Metal(3), Hide(1))
        ),
        FHBuilding(
             "67", "Lumber Depot", 0,
            listOf(Prosperity(3), Lumber(6), Metal(2), Hide(2), Coin(18)),
            listOf()
        ),
        FHBuilding(
             "67", "Lumber Depot", 1,
            listOf(Prosperity(7), Lumber(5), Metal(5), Hide(5)),
            listOf(Lumber(2), Metal(1), Hide(1))
        ),
        FHBuilding(
             "67", "Lumber Depot", 2,
            listOf(),
            listOf(Lumber(3), Metal(1), Hide(1))
        ),
        FHBuilding(
             "72", "Hide Depot", 0,
            listOf(Prosperity(3), Lumber(2), Metal(2), Hide(6), Coin(18)),
            listOf()
        ),
        FHBuilding(
             "72", "Hide Depot", 1,
            listOf(Prosperity(7), Lumber(5), Metal(5), Hide(5)),
            listOf(Lumber(1), Metal(1), Hide(2))
        ),
        FHBuilding(
             "72", "Hide Depot", 2,
            listOf(),
            listOf(Lumber(1), Metal(1), Hide(3))
        ),
        FHBuilding(
             "74", "Tavern", 0,
            listOf(Prosperity(2), Lumber(2), Metal(2), Hide(1), Coin(18)),
            listOf()
        ),
        FHBuilding(
             "74", "Tavern", 1,
            listOf(Prosperity(4), Lumber(4), Metal(4), Hide(2)),
            listOf(Lumber(2), Metal(1))
        ),
        FHBuilding(
             "74", "Tavern", 2,
            listOf(Prosperity(6), Lumber(6), Metal(4), Hide(2)),
            listOf(Lumber(2), Metal(2))
        ),
        FHBuilding(
             "74", "Tavern", 3,
            listOf(),
            listOf(Lumber(2), Metal(2), Hide(2))
        ),
        FHBuilding(
             "81", "Hall of Revelry", 0,
            listOf(Prosperity(5), Lumber(6), Metal(6), Hide(6), Coin(18)),
            listOf()
        ),
        FHBuilding(
             "81", "Hall of Revelry", 1,
            listOf(),
            listOf(Lumber(2), Metal(2), Hide(2))
        ),
        FHBuilding(
             "81", "Hall of Revelry", 2,
            listOf(),
            listOf(Lumber(2), Metal(2), Hide(2))
        ),
        FHBuilding(
             "83", "Library", 0,
            listOf(Prosperity(2), Lumber(3), Metal(2), Coin(18)),
            listOf(Lumber(2), Metal(1))
        ),
        FHBuilding(
             "83", "Library", 1,
            listOf(Prosperity(4), Lumber(4), Metal(4), Hide(1)),
            listOf(Lumber(2), Metal(2))
        ),
        FHBuilding(
             "83", "Library", 2,
            listOf(Prosperity(6), Lumber(2), Metal(5), Hide(5)),
            listOf(Lumber(2), Metal(2), Hide(2))
        ),
        FHBuilding(
             "83", "Library", 3,
            listOf(),
            listOf(Lumber(2), Metal(1), Hide(1))
        ),
        FHBuilding(
             "84", "Workshop", 1,
            listOf(),
            listOf(Lumber(2), Metal(1), Hide(1))
        ),
        FHBuilding(
             "85", "Carpenter", 0,
            listOf(Prosperity(2), Lumber(4), Metal(3), Hide(2), Coin(18)),
            listOf()
        ),
        FHBuilding(
             "85", "Carpenter", 1,
            listOf(Prosperity(5), Lumber(6), Metal(5), Hide(4)),
            listOf(Lumber(2), Metal(1), Hide(1))
        ),
        FHBuilding(
             "85", "Carpenter", 2,
            listOf(),
            listOf(Lumber(3), Metal(2), Hide(2))
        ),
        FHBuilding(
             "88", "Stables", 0,
            listOf(Prosperity(2), Lumber(6), Metal(2), Hide(2), Coin(18)),
            listOf()
        ),
        FHBuilding(
             "88", "Stables", 1,
            listOf(Prosperity(4), Lumber(4), Metal(5), Hide(5)),
            listOf(Lumber(3), Metal(1))
        ),
        FHBuilding(
             "88", "Stables", 2,
            listOf(Prosperity(6), Lumber(6), Metal(7), Hide(6)),
            listOf(Lumber(3), Metal(1), Hide(1))
        ),
        FHBuilding(
             "88", "Stables", 3,
            listOf(Prosperity(8), Lumber(8), Metal(8), Hide(8), Morale(1)),
            listOf(Lumber(3), Metal(2), Hide(2))
        ),
        FHBuilding(
             "88", "Stables", 4,
            listOf(),
            listOf(Lumber(3), Metal(3), Hide(3))
        ),
        FHBuilding(
             "90", "Town Hall", 0,
            listOf(Prosperity(2), Lumber(2), Metal(2), Hide(1), Coin(18)),
            listOf()
        ),
        FHBuilding(
             "90", "Town Hall", 1,
            listOf(Prosperity(4), Lumber(3), Metal(3), Hide(3)),
            listOf(Lumber(2), Metal(2))
        ),
        FHBuilding(
             "90", "Town Hall", 2,
            listOf(Prosperity(6), Lumber(4), Metal(5), Hide(4)),
            listOf(Lumber(2), Metal(2), Hide(1))
        ),
        FHBuilding(
             "90", "Town Hall", 3,
            listOf(),
            listOf(Lumber(2), Metal(2), Hide(2))
        ),
        FHBuilding(
             "98", "Barracks", 1,
            listOf(),
            listOf(Lumber(1), Metal(1))
        ),
        FHBuilding(
             "98", "Barracks", 2,
            listOf(),
            listOf(Lumber(1), Metal(2), Hide(1))
        ),
        FHBuilding(
             "98", "Barracks", 3,
            listOf(),
            listOf(Lumber(1), Metal(2), Hide(1))
        ),
        FHBuilding(
             "98", "Barracks", 3,
            listOf(),
            listOf(Lumber(1), Metal(2), Hide(1))
        ),
        FHBuilding(
             "98", "Barracks", 4,
            listOf(),
            listOf(Lumber(1), Metal(3), Hide(1))
        ),
        FHBuilding(
            "A", "Boat", 0,
            listOf(Prosperity(1), Lumber(4), Metal(1), Hide(2)),
            listOf()
        ),
        FHBuilding(
            "A", "Boat", 1,
            listOf(),
            listOf()
        ),
        FHBuilding(
            "B", "Sled", 0,
            listOf(Prosperity(1), Lumber(3), Metal(2), Hide(1)),
            listOf()
        ),
        FHBuilding(
            "B", "Sled", 1,
            listOf(),
            listOf()
        ),
        FHBuilding(
            "C", "Climbing Gear", 0,
            listOf(Prosperity(1), Lumber(1), Metal(3), Hide(2)),
            listOf()
        ),
        FHBuilding(
            "C", "Climbing Gear", 1,
            listOf(),
            listOf()
        ),
        FHBuilding(
            "J", "Wall J", 0,
            listOf(Prosperity(1), Lumber(4), Coin(10)),
            listOf()
        ),
        FHBuilding(
            "J", "Wall J", 1,
            listOf(),
            listOf()
        ),
        FHBuilding(
            "K", "Wall K", 0,
            listOf(Prosperity(2), Lumber(3), Metal(2), Hide(2), Coin(10)),
            listOf()
        ),
        FHBuilding(
            "K", "Wall K", 1,
            listOf(),
            listOf()
        ),
        FHBuilding(
            "L", "Wall L", 0,
            listOf(Prosperity(3), Lumber(5), Metal(2), Hide(1), Coin(10)),
            listOf()
        ),
        FHBuilding(
            "L", "Wall L", 1,
            listOf(),
            listOf()
        ),
        FHBuilding(
            "M", "Wall M", 0,
            listOf(Prosperity(4), Lumber(4), Metal(3), Hide(3), Coin(10)),
            listOf()
        ),
        FHBuilding(
            "M", "Wall M", 1,
            listOf(),
            listOf()
        ),
        FHBuilding(
            "N", "Wall N", 0,
            listOf(Prosperity(6), Lumber(6), Metal(3), Hide(2), Coin(10)),
            listOf()
        ),
        FHBuilding(
            "N", "Wall N", 1,
            listOf(),
            listOf()
        ),
    )


}