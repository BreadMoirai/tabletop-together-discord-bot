package com.github.breadmoirai.discordtabletop.api.core.game

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

interface TabletopGame {
    val id: String
    val name: String
    val description: String
    val playerCount: IntRange

    fun createLobby(event: GenericCommandInteractionEvent)
}


