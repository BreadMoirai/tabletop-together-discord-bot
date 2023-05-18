package com.github.breadmoirai.discordtabletop.core.games.frosthaven.building

import com.github.breadmoirai.discordtabletop.core.games.frosthaven.FHIcons
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.FHResource
import com.github.breadmoirai.discordtabletop.discord.emoji
import com.github.breadmoirai.discordtabletop.storage.Storable
import com.github.breadmoirai.discordtabletop.storage.StorableId
import com.github.breadmoirai.discordtabletop.storage.StorableTransient


data class FHBuilding(
    val id: String,
    val name: String,
    val level: Int = 0,
    val upgradeCost: List<FHResource>,
    val rebuildCost: List<FHResource>,
    val wrecked: Boolean = false
) : Storable {
    @StorableId
    private val index = "$id-$level"

    @StorableTransient
    val front: String
        get() = getUrl(!wrecked)

    @StorableTransient
    val back: String
        get() = getUrl(wrecked)

    private fun getUrl(back: Boolean): String {
        return buildString {
            append(CARD_URL)
            append("/fh-")
            append(id.padStart(2, '0'))
            append("-")
            append(name.lowercase().replace(' ', '-'))
            append("-level-")
            append(level.coerceAtLeast(1))
            if (back)
                append("-back")
            append(".png")
        }
    }

    @StorableTransient
    val sticker: String
        get() = buildString {
            append(STICKER_URL)
            append("/fh-")
            append(id)
            append("-")
            append(name.lowercase().replace(' ', '-'))
            if (id.toIntOrNull() != null) {
                append("-l")
                append(level)
            }
            append(".png")
        }

    val formatted: String
        get() = buildString {
            // id
            append("`")
            if (id.toIntOrNull() != null) append(id.padStart(2, '0'))
            else append(id.padEnd(2, ' '))
            append("` ")
            // level
            if (level == 0)
                append(FHIcons.blank)
            else
                append(level.emoji.formatted)
            // name
            append("`")
            append(name.padEnd(24, ' '))
            append("`")
            // rebuild / upgrade cost
            if (wrecked) {
                append(FHIcons.wrecked.formatted)
                append("  ")
                for (cost in rebuildCost) {
                    append(cost.formatted)
                    append("   ")
                }
            }
            else if (upgradeCost.isNotEmpty()) {
                if (level == 0)
                    append(FHIcons.build.formatted)
                else
                    append(FHIcons.upgrade.formatted)
                append("  ")
                for (cost in upgradeCost) {
                    append(cost.formatted)
                    append("   ")
                }
            }


        }

    companion object {
        private const val CARD_URL =
            "https://github.com/any2cards/worldhaven/raw/master/images/outpost-building-cards/frosthaven"
        private const val STICKER_URL =
            "https://github.com/any2cards/worldhaven/raw/master/images/art/frosthaven/stickers/individual/outpost-stickers"
    }
}


