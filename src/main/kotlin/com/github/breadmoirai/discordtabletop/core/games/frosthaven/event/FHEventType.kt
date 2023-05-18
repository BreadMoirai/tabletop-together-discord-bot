package com.github.breadmoirai.discordtabletop.core.games.frosthaven.event

import com.github.breadmoirai.discordtabletop.storage.Storage
import jetbrains.exodus.bindings.BindingUtils

enum class FHEventType(val text: String, val folder: String, val abbr: String, val count: Int) {
    SUMMER_OUTPOST("Summer Outpost", "outpost", "soe", 65),
    SUMMER_ROAD("Summer Road", "road", "sre", 52),
    WINTER_OUTPOST("Winter Outpost", "outpost", "woe", 81),
    WINTER_ROAD("Winter Road", "road", "wre", 49),
    BOAT("Boat", "boat", "be", 19);

    companion object {
        init {
            Storage.registerPropertyType(
                { read -> valueOf(BindingUtils.readString(read)) },
                { write, value -> write.writeString(value.name) })
        }
    }
}