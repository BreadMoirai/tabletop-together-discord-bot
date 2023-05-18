package com.github.breadmoirai.discordtabletop.util

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

suspend inline fun <T> List<T>.oxfordAnd(crossinline transform: suspend (T) -> String): String {
    assert(isNotEmpty()) { "List may not be empty" }
    if (size == 1) return transform(single())
    val first = this.dropLast(1)
    val last = this.last()
    val preAnd =
        first.map { transform(it) }.joinToString(", ")
    val postAnd = transform(last)
    return if (first.size > 1) {
        "$preAnd, and $postAnd"
    } else {
        "$preAnd and $postAnd"
    }
}

inline fun <T> List<T>.boxfordAnd(crossinline transform: (T) -> String): String {
    assert(isNotEmpty()) { "List may not be empty" }
    if (size == 1) return transform(single())
    val first = this.dropLast(1)
    val last = this.last()
    val preAnd =
        first.joinToString(", ") { transform(it) }
    val postAnd = transform(last)
    return if (first.size > 1) {
        "$preAnd, and $postAnd"
    } else {
        "$preAnd and $postAnd"
    }
}

fun List<String>.oxfordAnd(): String {
    assert(isNotEmpty()) { "List may not be empty" }
    if (size == 1) return single()
    val first = this.dropLast(1).joinToString(", ")
    val last = this.last()
    return if (first.length > 1) {
        "$first, and $last"
    } else {
        "$first and $last"
    }
}

@kotlin.contracts.ExperimentalContracts
suspend inline fun buildString(crossinline builderAction: suspend StringBuilder.() -> Unit): String {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return StringBuilder().also { builderAction(it) }.toString()
}