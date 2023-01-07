package com.github.breadmoirai.discordtabletop.api.core.connect4

import com.github.breadmoirai.discordtabletop.api.core.game.GameLobby
import com.github.breadmoirai.discordtabletop.api.core.game.Player
import com.github.breadmoirai.discordtabletop.api.core.game.TabletopGame
import com.github.breadmoirai.discordtabletop.api.discord.EmojiColor
import com.github.breadmoirai.discordtabletop.api.discord.EmojiNumber
import com.github.breadmoirai.discordtabletop.api.discord.InteractionManager
import com.github.breadmoirai.discordtabletop.api.discord.emoji
import com.github.breadmoirai.discordtabletop.api.jda.GuildButtonInteractionEvent
import com.github.breadmoirai.discordtabletop.api.jda.GuildCommandInteractionEvent
import com.github.breadmoirai.discordtabletop.api.reactive.Cancellable
import dev.minn.jda.ktx.messages.InlineEmbed
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Instant

object ConnectFour : TabletopGame, KoinComponent {
    override val id = "connect4"
    override val name = "Connect 4"
    override val description = ""
    override val playerCount = 2..2

    override fun createLobby(event: GenericCommandInteractionEvent) {
        val ge = GuildCommandInteractionEvent(event)
        val gameLobby = GameLobby(ConnectFour, ge)
        gameLobby.availableColors.removeAll(listOf(EmojiColor.Black, EmojiColor.Brown, EmojiColor.White, EmojiColor.Orange))
        gameLobby.gameStarted.subscribe { scope, (event, players) ->
            // cancel()
            // should just get garbage collected as after the gameLobby closes
            ConnectFourSession(scope, event, players)
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectFourSession(scope: CoroutineScope, event: GuildButtonInteractionEvent, players: List<Player>) :
    KoinComponent {
    private val interactionManager by inject<InteractionManager>()
    private val board: Array<Array<Player?>> = Array(8) { Array(8) { null } }
    private val nextPlayer: ReceiveChannel<Player>
    private val _currentPlayer: MutableStateFlow<Player>
    private val currentPlayer: Player
        get() = _currentPlayer.value
    private lateinit var jobs: List<Cancellable>

    init {
        val (first, second) = players.shuffled()
        nextPlayer = scope.produce {
            while (true) {
                send(second)
                send(first)
            }
        }
        _currentPlayer = MutableStateFlow(first)
        scope.launch {
            init(event)
        }
    }

    private suspend fun init(event: GuildButtonInteractionEvent) {

        event.editMessage(MessageEdit {
            embed { buildBoard(false) }
            val buttons = EmojiNumber.ofRange(1..8).map { num ->
                val id = "${event.id}-${num.ordinal}"
                emoji(id, Emoji.fromUnicode(num.code))
            }
            actionRow(buttons.subList(0, 4))
            actionRow(buttons.subList(4, 8))
            jobs = EmojiNumber.ofRange(1..8).map { num ->
                val id = "${event.id}-${num.ordinal}"
                interactionManager.addButtonHandler(id) { _, press ->
                    if (currentPlayer.userId != press.member.idLong) {
                        press.reply("It's not your turn!").setEphemeral(true).queue()
                        return@addButtonHandler
                    }
                    // drop player token into column
                    val j = num.ordinal - 1
                    var i = 0
                    for (k in 0 until 8) {
                        i = k
                        if (board[i][j] == null) {
                            board[i][j] = currentPlayer
                            break
                        }
                    }
                    // check for win
                    // forward diagonal
                    var consecutiveCount = 0
                    for (d in -3..3) {
                        val x = i + d
                        val y = j + d
                        if (x !in 0 until 8)
                            continue
                        if (y !in 0 until 8)
                            continue
                        if (board[x][y] == currentPlayer) {
                            consecutiveCount++
                        } else {
                            consecutiveCount = 0
                        }
                        if (consecutiveCount == 4) {
                            endGame(press)
                            return@addButtonHandler
                        }
                    }
                    // backwards diagonal
                    consecutiveCount = 0
                    for (d in -3..3) {
                        val x = i + d
                        val y = j - d
                        if (x !in 0 until 8)
                            continue
                        if (y !in 0 until 8)
                            continue
                        if (board[x][y] == currentPlayer) {
                            consecutiveCount++
                        } else {
                            consecutiveCount = 0
                        }
                        if (consecutiveCount == 4) {
                            endGame(press)
                            return@addButtonHandler
                        }
                    }
                    // horizontal
                    consecutiveCount = 0
                    for (d in -3..3) {
                        val x = i
                        val y = j + d
                        if (y !in 0 until 8)
                            continue
                        if (board[x][y] == currentPlayer) {
                            consecutiveCount++
                        } else {
                            consecutiveCount = 0
                        }
                        if (consecutiveCount == 4) {
                            endGame(press)
                            return@addButtonHandler
                        }
                    }
                    // vertical
                    consecutiveCount = 0
                    for (d in -3..3) {
                        val x = i + d
                        val y = j
                        if (x !in 0 until 8)
                            continue
                        if (board[x][y] == currentPlayer) {
                            consecutiveCount++
                        } else {
                            consecutiveCount = 0
                        }
                        if (consecutiveCount == 4) {
                            endGame(press)
                            return@addButtonHandler
                        }
                    }

                    // no win
                    _currentPlayer.update { nextPlayer.receive() }
                    press.editMessage(MessageEdit {
                        embed { buildBoard(false) }
                    }).queue()

                }
            }
        }).queue()
    }

    private suspend fun endGame(press: GuildButtonInteractionEvent) {
        press.editMessage(MessageEdit(components = listOf()) {
            embed {
                buildBoard(true)
            }
        }).queue()
        jobs.forEach { it.cancel() }
    }

    private suspend fun InlineEmbed.buildBoard(gameEnded: Boolean) {
        title = "Connect 4"
        if (gameEnded) {
            author {
                name = currentPlayer.member().effectiveName
                iconUrl = currentPlayer.member().effectiveAvatarUrl
            }
        }
        val (second, first) = nextPlayer.receiveAsFlow().take(2).map { "${it.color.code} ${it.member().effectiveName}" }.toList()
        if (gameEnded) {
            description = "**Winner: ${first}**\nLoser: $second"
        } else {
            description = "${first}\n${second}"
        }
        field {
            value = board.reversed().joinToString("\n") { row ->
                row.joinToString("") { player -> player?.color?.code ?: EmojiColor.Black.code }
            } + "\n" + EmojiNumber.ofRange(1..8).joinToString("") { it.code }
        }
        timestamp = Instant.now()
    }
}