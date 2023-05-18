package com.github.breadmoirai.discordtabletop.core.games.frosthaven.event

object FHEvents {

    fun startingDeck(id: String): FHEventDeck {
        return FHEventDeck(id, startingEvents)
    }

    fun getEvents(type: FHEventType, range: IntRange): List<FHEvent> {
        assert(range.first > 1) { "range start must be positive non-zero number" }
        assert(range.last <= type.count) { "range end must be leq to ${type.count} for event type ${type.name}"}
        return range.map { FHEvent(type, it) }
    }

    val startingEvents: Map<FHEventType, List<FHEvent>> = mapOf(
        FHEventType.SUMMER_OUTPOST to getEvents(FHEventType.SUMMER_OUTPOST, 1..20),
        FHEventType.SUMMER_ROAD to getEvents(FHEventType.SUMMER_ROAD, 1..20),
        FHEventType.WINTER_OUTPOST to getEvents(FHEventType.WINTER_OUTPOST, 1..20),
        FHEventType.WINTER_ROAD to getEvents(FHEventType.WINTER_ROAD, 1..20),
        FHEventType.BOAT to listOf()
    )


}