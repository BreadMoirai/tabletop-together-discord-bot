package com.github.breadmoirai.discordtabletop.api.reactive.impl

import com.github.breadmoirai.discordtabletop.api.reactive.Cancellable
import com.github.breadmoirai.discordtabletop.api.reactive.Event
import dev.minn.jda.ktx.events.getDefaultScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class EventImpl<T> : Event<T> {

    private val _events = MutableSharedFlow<T>()
    override val events = _events.asSharedFlow()
    override suspend fun invokeEvent(event: T) = _events.emit(event)

    override fun subscribe(
        scope: CoroutineScope,
        onEvent: suspend Cancellable.(CoroutineScope, T) -> Unit
    ): Cancellable {
        val job = CancellableJob { job ->
            _events.collect { value ->
                onEvent(job, scope, value)
            }
        }
        scope.launch {
            job.start(this)
        }
        return job
    }

    override fun subscribe(onEvent: suspend Cancellable.(CoroutineScope, T) -> Unit): Cancellable {
        return subscribe(getDefaultScope(), onEvent)
    }


    override fun subscribe(scope: CoroutineScope, onEvent: suspend Cancellable.(T) -> Unit): Cancellable {
        return subscribe(scope) { scope, t ->
            onEvent(this, t)
        }
    }

    override fun subscribe(onEvent: suspend Cancellable.(T) -> Unit): Cancellable {
        return subscribe(getDefaultScope()) { _, t ->
            onEvent(this, t)
        }
    }

    override fun close() {

    }
}