package com.github.breadmoirai.discordtabletop.api.reactive

import com.github.breadmoirai.discordtabletop.api.reactive.impl.EventImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow

fun <T : Any> event(): Event<T> {
    return EventImpl()
}

interface Event<T> {

    val events: SharedFlow<T>

    suspend fun invokeEvent(event: T)

    fun subscribe(scope: CoroutineScope, onEvent: suspend Cancellable.(T) -> Unit): Cancellable

    fun subscribe(scope: CoroutineScope, onEvent: suspend Cancellable.(CoroutineScope, T) -> Unit): Cancellable

    fun subscribe(onEvent: suspend Cancellable.(CoroutineScope, T) -> Unit): Cancellable

    fun subscribe(onEvent: suspend Cancellable.(T) -> Unit): Cancellable

    fun close()
}
