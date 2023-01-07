package com.github.breadmoirai.discordtabletop.api.reactive

import com.github.breadmoirai.discordtabletop.api.reactive.impl.StateImpl
import kotlinx.coroutines.CoroutineScope

fun <T : Any> state(initialValue: T): State<T> {
    return StateImpl(initialValue)
}

interface State<T : Any> {
    val value: T

    suspend fun set(newValue: T)

    suspend fun subscribe(scope: CoroutineScope, onChange: Cancellable.(T) -> Unit): Cancellable
}