package com.github.breadmoirai.discordtabletop.reactive

interface Cancellable {
    suspend fun cancel()
}

