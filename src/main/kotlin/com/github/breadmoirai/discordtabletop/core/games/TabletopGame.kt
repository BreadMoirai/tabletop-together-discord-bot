package com.github.breadmoirai.discordtabletop.core.games

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

interface TabletopGame {
    val id: String
    val name: String
    val description: String
    val playerCount: IntRange

    suspend fun openLobby(event: GenericCommandInteractionEvent)
}


