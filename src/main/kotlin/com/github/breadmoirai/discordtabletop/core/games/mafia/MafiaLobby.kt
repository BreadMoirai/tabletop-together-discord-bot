package com.github.breadmoirai.discordtabletop.core.games.mafia

import com.github.breadmoirai.discordtabletop.core.InteractableSession.Companion.randomId
import com.github.breadmoirai.discordtabletop.core.games.GameLobby
import com.github.breadmoirai.discordtabletop.discord.emoji
import dev.minn.jda.ktx.interactions.components.danger
import dev.minn.jda.ktx.interactions.components.success
import dev.minn.jda.ktx.messages.InlineEmbed
import dev.minn.jda.ktx.messages.InlineMessage
import io.ktor.util.*
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import kotlin.time.Duration.Companion.hours

class MafiaLobby(commandEvent: GenericCommandInteractionEvent) :
    GameLobby(Mafia, 24.hours, commandEvent) {
//    val playSet: MafiaPlaySet = MafiaPlaySet()

    val gameId = randomId("mafia")
    private val editId = randomId("edit-game")
    override suspend fun launch() {
        bindButton(editId, ::onEditGame)
        super.launch()
    }

    private suspend fun onEditGame(event: ButtonInteractionEvent) {

    }

    override fun addActionRows(): suspend InlineMessage<MessageCreateData>.() -> Unit = {
        actionRow(
            success(joinId, "Join"),
            danger(leaveId, "Leave"),
        )
        actionRow(
            emoji(startId, Emoji.fromUnicode("⚔️"), "Start Game"),
            emoji(editId, Emoji.fromUnicode("\uD83D\uDCCB"), "Edit Roles"),
            emoji(cancelId, Emoji.fromUnicode("\uD83D\uDDD1️"), "Cancel Game")
        )
    }

    override suspend fun buildEmbed(): suspend InlineEmbed.() -> Unit = {
        super.buildEmbed().invoke(this)

    }

}