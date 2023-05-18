package com.github.breadmoirai.discordtabletop.core.games

import com.github.breadmoirai.discordtabletop.core.InteractableSession.Companion.randomId
import com.github.breadmoirai.discordtabletop.discord.emoji
import com.github.breadmoirai.discordtabletop.reactive.Event
import com.github.breadmoirai.discordtabletop.reactive.event
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.danger
import dev.minn.jda.ktx.interactions.components.success
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.InlineEmbed
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.time.Instant
import kotlin.time.Duration

class LobbyMember(val member: Member) : MemberRef {
    override fun member(): Member {
        return member
    }
}

abstract class GameLobby(
    val game: TabletopGame,
    inactivityLimit: Duration,
    commandEvent: GenericCommandInteractionEvent
) : com.github.breadmoirai.discordtabletop.core.BaseInteractableSession(inactivityLimit, listOf(), commandEvent) {

    init {
        assert(commandEvent.isFromGuild) { "Lobby may only be started in a guild" }
        assert(commandEvent.channelType == ChannelType.TEXT) { "Lobby must be started in a Text Channel" }
    }

    val owner = commandEvent.member!!
    val startEvent: IReplyCallback = commandEvent
    val playerJoined: Event<Member> = event()
    val playerLeft: Event<Member> = event()
    val gameStarted: Event<ButtonInteractionEvent> = event()
    private val _players: MutableList<Member> = mutableListOf()
    val players: List<Member>
        get() = _players

    protected val joinId = randomId("join-game")
    protected val leaveId = randomId("leave-game")
    protected val startId = randomId("start-game")
    protected val cancelId = randomId("cancel-game")


    protected open suspend fun onPlayerJoin(event: ButtonInteractionEvent) {
        logger.info(this, "${event.member!!.effectiveName} attempted to join lobby")
        if (players.any { player -> player.user.idLong == event.user.idLong }) {
            event.reply("You cannot join a lobby that you are already in").setEphemeral(true).queue()
            return
        }
        if (game.playerCount.last == players.size) {
            event.reply("You cannot join a full lobby").setEphemeral(true).queue()
            return
        }
        val player = event.member!!
        _players.add(player)
        playerJoined.invokeEvent(player)
        logger.info(this, "${event.member!!.effectiveName} successfully joined lobby")
        event.editMessageEmbeds(Embed {
            buildEmbed().invoke(this)
        }).queue()
    }

    protected open suspend fun onPlayerLeave(event: ButtonInteractionEvent) {
        logger.info(this, "${event.member!!.effectiveName} attempted to leave lobby")
        val player = players.find { player -> player.user.idLong == event.user.idLong }
        if (player == null) {
            event.reply("You cannot leave a lobby that you are not in").setEphemeral(true).queue()
            return
        }
        _players.remove(player)
        logger.info(this, "${event.member!!.effectiveName} successfully left lobby")
        event.editMessageEmbeds(Embed {
            buildEmbed().invoke(this)
        }).queue()
        playerLeft.invokeEvent(player)
    }

    protected open suspend fun onStartGame(event: ButtonInteractionEvent) {
        logger.info(this, "${event.member!!.effectiveName} attempted to start game")
        if (players.size !in game.playerCount) {
            event.reply(
                "Cannot start game with ${players.size} players. " +
                        "Required players is ${game.playerCount.first} to ${game.playerCount.last}"
            ).setEphemeral(true).queue()
            return
        }
        logger.info(this, "${event.member!!.effectiveName} successfully started game")
        event.editComponents(listOf()).queue()
        cleanup()
        gameStarted.invokeEvent(event)
    }

    protected open suspend fun onCancelGame(event: ButtonInteractionEvent) {
        logger.info(this, "${event.member!!.effectiveName} attempted to cancel game")
        cleanup()
        logger.info(this, "${event.member!!.effectiveName} successfully cancelled game")
        event.editMessage(cancelledGame).queue()
        gameCancelled.invokeEvent(Unit)
    }

    override suspend fun launch() {
        logger.setContext(this, "${game.name}:Lobby:${startEvent.id}")
        bindButton(joinId, ::onPlayerJoin)
        bindButton(leaveId, ::onPlayerLeave)
        bindButton(startId, ::onStartGame)
        bindButton(cancelId, ::onCancelGame)
        val hook = startEvent.reply(MessageCreate {
            embed {
                buildEmbed().invoke(this)
            }
            addActionRows().invoke(this)
        }).await()
        val message = hook.retrieveOriginal().await()
        messageId = message.idLong
    }

    protected open fun addActionRows(): suspend InlineMessage<MessageCreateData>.() -> Unit = {
        actionRow(
            success(joinId, "Join"),
            danger(leaveId, "Leave"),
            emoji(startId, Emoji.fromUnicode("⚔️"), "Start Game"),
            emoji(cancelId, Emoji.fromUnicode("\uD83D\uDDD1️"), "Cancel Game")
        )
    }

    protected open suspend fun buildEmbed(): suspend InlineEmbed.() -> Unit = {
        title = game.name
        description = game.description
        field {
            name = "Players"
            value = players.joinToString("\n") { this@GameLobby.displayForLobby(it) }
            inline = true
        }
        footer {
            name = owner.effectiveName
            iconUrl = owner.effectiveAvatarUrl
        }
        timestamp = Instant.now()
    }

    protected open fun displayForLobby(member: Member): String {
        val idx = players.indexOf(member) + 1
        return "${idx.emoji.formatted} ${member.effectiveName}"
    }


}