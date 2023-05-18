@file:Suppress("unused")

package com.github.breadmoirai.discordtabletop.discord

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import kotlinx.coroutines.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.SubscribeEvent
import kotlin.coroutines.resume
import kotlin.time.Duration

suspend inline fun <reified T : GenericEvent> JDA.await(timeout: Duration, crossinline filter: (T) -> Boolean = { true }) = suspendCancellableCoroutine<Option<T>> {
    val listener = object : EventListener {
        @SubscribeEvent
        override fun onEvent(event: GenericEvent) {
            if (event is T && filter(event)) {
                removeEventListener(this)
                it.resume(event.some())
            }
        }
    }
    addEventListener(listener)
    @OptIn(DelicateCoroutinesApi::class)
    val timer = GlobalScope.launch {
        delay(timeout)
        removeEventListener(listener)
        it.resume(None)
    }
    it.invokeOnCancellation {
        timer.cancel()
        removeEventListener(listener)
    }
}