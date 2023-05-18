package com.github.breadmoirai.discordtabletop.util

operator fun <E> List<E>.get(intRange: IntRange): List<E> {
    return subList(intRange.first, intRange.last + 1)
}