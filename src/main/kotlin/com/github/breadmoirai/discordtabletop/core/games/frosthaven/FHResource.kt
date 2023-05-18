package com.github.breadmoirai.discordtabletop.core.games.frosthaven

import com.github.breadmoirai.discordtabletop.core.games.frosthaven.FHIcons.morale1
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.FHIcons.morale2
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.FHIcons.morale3
import com.github.breadmoirai.discordtabletop.storage.Storable
import com.github.breadmoirai.discordtabletop.storage.StorableId
import com.github.breadmoirai.discordtabletop.storage.StorableTransient

sealed class FHResource(@StorableId val name: String, val count: Int) : Storable {
    class Prosperity(count: Int) : FHResource("Prosperity", count)
    class Coin(count: Int) : FHResource("Coin", count)
    class Hide(count: Int) : FHResource("Hide", count)
    class Lumber(count: Int) : FHResource("Lumber", count)
    class Metal(count: Int) : FHResource("Metal", count)
    class Arrowvine(count: Int) : FHResource("Arrowvine", count)
    class Axenut(count: Int) : FHResource("Axenut", count)
    class Corpsecap(count: Int) : FHResource("Corpsecap", count)
    class Flamefruit(count: Int) : FHResource("Flamefruit", count)
    class Rockroot(count: Int) : FHResource("Rockroot", count)
    class Snowthistle(count: Int) : FHResource("Snowthistle", count)
    class Morale(count: Int) : FHResource("Morale", count)
    @StorableTransient
    val formatted: String
        get() {
            return when (this) {
                is Arrowvine -> "`$count` ${FHIcons.arrowvine.formatted}"
                is Axenut -> "`$count` ${FHIcons.axenut.formatted}"
                is Corpsecap -> "`$count` ${FHIcons.corpsecap.formatted}"
                is Flamefruit -> "`$count` ${FHIcons.flamefruit.formatted}"
                is Rockroot -> "`$count` ${FHIcons.rockroot.formatted}"
                is Snowthistle -> "`$count` ${FHIcons.snowthistle.formatted}"
                is Hide -> "`$count` ${FHIcons.hide.formatted}"
                is Metal -> "`$count` ${FHIcons.metal.formatted}"
                is Lumber -> "`$count` ${FHIcons.lumber.formatted}"
                is Coin -> "`$count` ${FHIcons.coin.formatted}"
                is Prosperity -> "`$count` ${FHIcons.prosperity.formatted}"
                is Morale -> listOf(morale1, morale2, morale3).joinToString { it.formatted }
            }
        }
}



