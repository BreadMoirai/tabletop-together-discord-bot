package com.github.breadmoirai.discordtabletop.api.core.game

import com.github.breadmoirai.discordtabletop.api.discord.EmojiColor
import com.github.breadmoirai.discordtabletop.api.discord.InteractionManager
import com.github.breadmoirai.discordtabletop.api.discord.emoji
import com.github.breadmoirai.discordtabletop.api.jda.GuildButtonInteractionEvent
import com.github.breadmoirai.discordtabletop.api.jda.GuildCommandInteractionEvent
import com.github.breadmoirai.discordtabletop.api.logging.logger
import com.github.breadmoirai.discordtabletop.api.reactive.Cancellable
import com.github.breadmoirai.discordtabletop.api.reactive.Event
import com.github.breadmoirai.discordtabletop.api.reactive.event
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.danger
import dev.minn.jda.ktx.interactions.components.success
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.InlineEmbed
import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.entities.emoji.Emoji
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Instant

class GameLobby(val game: TabletopGame, event: GuildCommandInteractionEvent) : KoinComponent {

    private val logger by logger<GameLobby>()

    val availableColors: MutableList<EmojiColor> = EmojiColor.values().toMutableList()

    init {
        availableColors.shuffle()
    }

    val playerJoined: Event<Player> = event()
    val playerLeft: Event<Player> = event()
    val gameStarted: Event<Pair<GuildButtonInteractionEvent, List<Player>>> = event()
    val gameCancelled: Event<Unit> = event()
    val owner = event.member

    private val interactionManager: InteractionManager by inject()
    private val _players: MutableList<Player> = mutableListOf()
    val players: List<Player>
        get() = _players

    private val mutex = Mutex()

    private val listeners: MutableList<Cancellable> = mutableListOf()

    init {
        runBlocking {
            val message = event.reply(MessageCreate {
                embed {
                    buildEmbed()
                }
                actionRow(
                    success("${event.id}join-game", "Join"),
                    danger("${event.id}leave-game", "Leave")
                )
                actionRow(
                    emoji("${event.id}start-game", Emoji.fromUnicode("⚔️"), "Start Game"),
                    emoji("${event.id}cancel-game", Emoji.fromUnicode("\uD83D\uDDD1️"), "Cancel Game")
                )
            }).await()

            listeners += interactionManager.addButtonHandler("${event.id}join-game") { _, event ->
//                if (players.any { player -> player.userId == event.user.idLong }) {
//                    event.reply("You cannot join a lobby that you are already in").setEphemeral(true).queue()
//                    return@addButtonHandler
//                }
                if (game.playerCount.last == players.size) {
                    event.reply("You cannot join a full lobby").setEphemeral(true).queue()
                    return@addButtonHandler
                }
                logger.info(
                    event,
                    "Player ${event.member.effectiveName} joined ${game.name} as ${availableColors[0].name}"
                )
                val player = Player.from(event.member, availableColors.removeFirst())
                _players.add(player)
                event.editMessageEmbeds(Embed {
                    buildEmbed()
                }).queue()
                playerJoined.invokeEvent(player)
            }
            listeners += interactionManager.addButtonHandler("${event.id}leave-game") { _, event ->
                val player = players.find { player -> player.userId == event.user.idLong }
                if (player == null) {
                    event.reply("You cannot leave a lobby that you are not in").setEphemeral(true).queue()
                    return@addButtonHandler
                }
                logger.info(
                    event,
                    "Player ${player.member().effectiveName} left ${game.name} as ${player.color}"
                )
                _players.remove(player)
                availableColors.add(player.color)
                availableColors.shuffle()
                event.editMessageEmbeds(Embed {
                    buildEmbed()
                }).queue()
                playerLeft.invokeEvent(player)
            }
            listeners += interactionManager.addButtonHandler("${event.id}cancel-game") { _, event ->
                mutex.withLock {
                    if (closeLobby()) return@addButtonHandler
                }
                event.message.delete().queue()
                event.reply("Game Cancelled").setEphemeral(true).queue()
                gameCancelled.invokeEvent(Unit)
            }
            listeners += interactionManager.addButtonHandler("${event.id}start-game") { _, event ->
                if (players.size !in game.playerCount) {
                    event.reply("Cannot start game with ${players.size} players. Required players is ${game.playerCount.first} to ${game.playerCount.last}").queue()
                    return@addButtonHandler
                }
                mutex.withLock {
                    if (closeLobby()) return@addButtonHandler
                }
                gameStarted.invokeEvent(event to players)
            }
        }
    }

    private suspend fun closeLobby(): Boolean {
        if (listeners.isEmpty()) return true
        listeners.forEach { it.cancel() }
        listeners.clear()
        return false
    }

    private fun InlineEmbed.buildEmbed() {
        title = game.name
        description = game.description
        field {
            name = "Players"
            value = players.joinToString("\n") { "${it.color.code} ${it.member.effectiveName}" }
        }
        footer {
            name = owner.effectiveName
            iconUrl = owner.effectiveAvatarUrl

        }
        timestamp = Instant.now()
    }


}