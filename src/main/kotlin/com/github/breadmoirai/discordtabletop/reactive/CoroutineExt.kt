@file:OptIn(DelicateCoroutinesApi::class)

package com.github.breadmoirai.discordtabletop.reactive

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import kotlin.reflect.KProperty

fun Job.invokeOnCancellation(cancellationHandler: (CancellationException) -> Unit): Job {
    invokeOnCompletion { exception ->
        if (exception is CancellationException) {
            cancellationHandler(exception)
        }
    }
    return this
}

class BlockingDelegate<T>(func: suspend () -> T) {

    private val value = runBlocking { func() }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }
}

fun <T> blocking(func: suspend () -> T): BlockingDelegate<T> {
    return BlockingDelegate(func)
}

class AsyncDelegate<T>(func: suspend () -> T) {

    private val job = GlobalScope.async {
        func()
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return runBlocking { job.await() }
    }
}

fun <T> async(func: suspend () -> T): AsyncDelegate<T> {
    return AsyncDelegate(func)
}

suspend fun <L, R> List<Pair<L, Deferred<R>>>.awaitAll(): List<Pair<L, R>> {
    val (ls, deferreds) = unzip()
    val results = deferreds.awaitAll()
    return ls.zip(results)
}