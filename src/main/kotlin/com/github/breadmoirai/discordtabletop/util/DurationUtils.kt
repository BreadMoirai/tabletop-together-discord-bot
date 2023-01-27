package com.github.breadmoirai.discordtabletop.util

import kotlin.time.Duration

fun Duration.toReadableString(): String {
    val millisValue = this.inWholeMilliseconds
    if (millisValue == 0L) {
        return "0ms"
    }

    val days = inWholeDays
    val hours = inWholeHours % 24

    val minutes = inWholeMinutes % 60
    val seconds = inWholeSeconds % 60
    val millis = inWholeMilliseconds % 1000

    val elements = listOf(
        "$days days",
        "$hours hours",
        "$minutes minutes",
        "$seconds seconds"/*, "${millis}ms"*/
    ).filter { it[0] != '0' }.map { if (it.startsWith("1 ")) it.dropLast(1) else it }
    return elements.oxfordAnd()
}