package com.github.breadmoirai.discordtabletop.reactive.impl

import com.github.breadmoirai.discordtabletop.reactive.Cancellable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class CancellableJob(private val runnable: suspend (Cancellable) -> Unit) : Cancellable {

    private lateinit var job: Job

    suspend fun start(scope: CoroutineScope): Cancellable {
        assert(!::job.isInitialized)
        job = scope.launch {
            runnable(this@CancellableJob)
        }
        return this@CancellableJob
    }

    override suspend fun cancel() {
        job.cancel("Job Cancelled")
    }
}

class CancellableImpl(private val runnable: suspend (Cancellable) -> Unit, private val onCancel: suspend () -> Unit) : Cancellable {
    override suspend fun cancel() {
        onCancel()
    }
}

class CancellableConsumer<T>(private val runnable: suspend (Cancellable, T) -> Unit, private val onCancel: suspend () -> Unit) : Cancellable {
    suspend fun accept(value: T) {
        runnable(this, value)
    }
    override suspend fun cancel() {
        onCancel()
    }
}