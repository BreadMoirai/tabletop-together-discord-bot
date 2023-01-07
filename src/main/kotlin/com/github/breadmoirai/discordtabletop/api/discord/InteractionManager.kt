package com.github.breadmoirai.discordtabletop.api.discord

import com.github.breadmoirai.discordtabletop.api.jda.GuildButtonInteractionEvent
import com.github.breadmoirai.discordtabletop.api.jda.GuildEntitySelectInteractionEvent
import com.github.breadmoirai.discordtabletop.api.jda.GuildStringSelectInteractionEvent
import com.github.breadmoirai.discordtabletop.api.reactive.Cancellable

interface InteractionManager {
    fun addButtonHandler(
        componentId: String,
        onButton: suspend (Cancellable, GuildButtonInteractionEvent) -> Unit
    ): Cancellable

    fun addButtonHandler(
        componentId: String,
        messageId: Long,
        onButton: suspend (Cancellable, GuildButtonInteractionEvent) -> Unit
    ): Cancellable

    fun addStringSelectMenuHandler(
        componentId: String,
        onSelect: suspend (Cancellable, GuildStringSelectInteractionEvent) -> Unit
    ): Cancellable

    fun addStringSelectMenuHandler(
        componentId: String,
        messageId: Long,
        onSelect: suspend (Cancellable, GuildStringSelectInteractionEvent) -> Unit
    ): Cancellable

    fun addEntitySelectMenuHandler(
        componentId: String,
        onSelect: suspend (Cancellable, GuildEntitySelectInteractionEvent) -> Unit
    ): Cancellable

    fun addEntitySelectMenuHandler(
        componentId: String,
        messageId: Long,
        onSelect: suspend (Cancellable, GuildEntitySelectInteractionEvent) -> Unit
    ): Cancellable
}