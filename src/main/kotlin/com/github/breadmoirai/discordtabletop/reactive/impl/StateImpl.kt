package com.github.breadmoirai.discordtabletop.reactive.impl

import com.github.breadmoirai.discordtabletop.reactive.Cancellable
import com.github.breadmoirai.discordtabletop.reactive.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking

class StateImpl<T : Any>(initialValue: T) : State<T> {
    private val _state = MutableSharedFlow<T>(2)

    init {
        runBlocking {
            _state.emit(initialValue)
        }
    }

    override val value: T
        get() = _state.replayCache.last()

    override suspend fun set(newValue: T) {
        _state.emit(newValue)
    }

    override suspend fun subscribe(scope: CoroutineScope, onChange: suspend Cancellable.(T) -> Unit): Cancellable {
        return CancellableJob { self ->
            _state.collect { value ->
                self.onChange(value)
            }
        }.start(scope)
    }

}