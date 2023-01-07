package com.github.breadmoirai.discordtabletop.api.reactive

interface Cancellable {
    suspend fun cancel()
}

