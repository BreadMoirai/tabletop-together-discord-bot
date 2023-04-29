package com.github.breadmoirai.discordtabletop.core.games.mafia

import com.github.breadmoirai.discordtabletop.core.games.TabletopGame
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

object Mafia : TabletopGame {
    override val id: String = "mafia"
    override val name: String = "Mafia"
    override val description: String = "Mafia"
    override val playerCount: IntRange = 3..24

    val openLobbies: MutableMap<String, MafiaLobby> = mutableMapOf()
    val roles: MutableMap<String, MafiaRole> = mutableMapOf()
    val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    override suspend fun openLobby(event: GenericCommandInteractionEvent) {
        val gameLobby = MafiaLobby(event)
        openLobbies[gameLobby.gameId] = gameLobby
        gameLobby.launch()
        gameLobby.gameStarted.subscribe { _, startGameEvent ->
            println("Game started")
            // cancel()
            // should just get garbage collected as after the gameLobby closes
//            ONWSession(
//                startGameEvent,
//                gameLobby.players,
//                gameLobby.roles,
//                gameLobby.nightTimer,
//                5.minutes
//            ).launch()
        }
    }


    val requiredPermissions = listOf(
        Permission.VIEW_CHANNEL,
        Permission.MESSAGE_SEND,
        Permission.MESSAGE_SEND_IN_THREADS,
        Permission.MANAGE_THREADS,
        Permission.CREATE_PRIVATE_THREADS,
        Permission.NICKNAME_MANAGE,
        Permission.VOICE_MUTE_OTHERS
    )
}