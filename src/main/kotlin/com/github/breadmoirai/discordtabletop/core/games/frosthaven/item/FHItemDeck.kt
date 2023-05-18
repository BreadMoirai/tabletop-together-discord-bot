package com.github.breadmoirai.discordtabletop.core.games.frosthaven.item

import arrow.core.Option
import arrow.core.toOption
import com.github.breadmoirai.discordtabletop.storage.Storable
import com.github.breadmoirai.discordtabletop.storage.StorableId

class FHItemDeck(
    @StorableId val id: String,
    items: List<FHItem>,
    randomItems: List<FHItem>,
    randomBlueprints: List<FHItem>,
) : Storable {
    val items: MutableList<FHItem> = items.toMutableList()
    val randomItems: MutableList<FHItem> = randomItems.toMutableList()
    val randomBlueprints: MutableList<FHItem> = randomBlueprints.toMutableList()

    fun drawItem(): Option<FHItem> {
        val item = randomItems.randomOrNull().toOption()
        item.tap { randomItems.remove(it) }
        return item
    }

    fun drawBlueprint(): Option<FHItem> {
        val item = randomBlueprints.randomOrNull().toOption()
        item.tap { randomBlueprints.remove(it) }
        return item
    }

    fun addRandomBlueprints(toAdd: List<FHItem>): Option<FHItem> {
        val item = randomBlueprints.randomOrNull().toOption()
        item.tap { randomBlueprints.remove(it) }
        return item
    }

    val formatted: String
        get() = buildString {
            append("Available Items: ")

            var prev = -1
            var inRange = false
            for (i in items.map(FHItem::num).mapNotNull(String::toIntOrNull).sorted()) {
                if (prev + 1 == i) {
                    if (!inRange)
                        append("-")
                    inRange = true
                } else if (inRange) {
                    inRange = false
                    append(prev)
                    append(", ")
                    append(i)
                } else {
                    if (prev != -1)
                        append(", ")
                    append(i)
                }
                prev = i
            }
            if (inRange) {
                append(prev)
            }
        }
}