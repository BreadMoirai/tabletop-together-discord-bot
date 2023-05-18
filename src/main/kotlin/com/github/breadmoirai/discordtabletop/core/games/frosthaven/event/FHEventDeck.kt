package com.github.breadmoirai.discordtabletop.core.games.frosthaven.event

import arrow.core.Option
import arrow.core.firstOrNone
import arrow.core.some
import com.github.breadmoirai.discordtabletop.discord.bold
import com.github.breadmoirai.discordtabletop.storage.Storable
import com.github.breadmoirai.discordtabletop.storage.StorableId
import com.github.breadmoirai.discordtabletop.util.boxfordAnd
import com.github.breadmoirai.discordtabletop.util.oxfordAnd

class FHEventDeck(
    @StorableId val id: String,
    events: Map<FHEventType, List<FHEvent>>
) : Storable {
    val events: Map<FHEventType, MutableList<FHEvent>> = events.mapValues { (_, list) -> list.toMutableList() }

    fun drawEvent(type: FHEventType): Option<FHEvent> {
        val draw = events[type]!!.firstOrNone()
        draw.tap { events[type]!!.remove(it) }
        return draw
    }

    fun returnEvent(event: FHEvent) {
        events[event.type]!!.add(event)
    }

    fun addEvent(event: FHEvent) {
        events[event.type]!!.apply {
            add(event)
            shuffle()
        }
    }

    val formatted: String
        get() = buildString {
            append("Events: ".bold())
            append(events.entries.toList().boxfordAnd { (type, events) ->
                "${events.size} ${type.text}"
            })
        }
}