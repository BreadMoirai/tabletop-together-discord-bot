package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf

import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.some
import com.github.breadmoirai.discordtabletop.core.InteractableSession.Companion.randomId
import com.github.breadmoirai.discordtabletop.core.awaitButtonPress
import com.github.breadmoirai.discordtabletop.core.games.GameSession
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.OneNightWerewolfRole
import com.github.breadmoirai.discordtabletop.util.oxfordAnd
import com.github.breadmoirai.discordtabletop.util.toReadableString
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.primary
import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.util.concurrent.CompletableFuture
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class OneNightWerewolfSession(
    startEvent: ButtonInteractionEvent,
    playerList: List<Member>,
    roles: List<OneNightWerewolfRole>,
    val nightTimer: Duration,
    val dayTimer: Duration,
    val voteTimer: Duration
) : GameSession<OneNightWerewolfPlayer>(30.minutes, playerList.map { it.idLong }, startEvent) {

    val teams = mutableListOf(VillagerTeam(), WerewolfTeam(), TannerTeam())
    val roleBag = roles.shuffled().toMutableList()
    override val players = playerList.mapIndexed { i, member ->
        val role = roleBag.removeFirst()
        val team = getTeamForRole(role)
        OneNightWerewolfPlayer(
            member.user.idLong,
            channel.idLong,
            member.guild.idLong,
            this,
            i,
            role,
            role,
            team
        )
    }
    val center = roleBag.mapIndexed { i, role ->
        CenterCard(i + 1, this, role, getTeamForRole(role))
    }
    val actions = mutableListOf<NightAction>()
    val id = startEvent.id
    lateinit var playersToWake: MutableList<OneNightWerewolfPlayer>
    private val startJobs = mutableListOf<Deferred<Option<CompletableFuture<Message>>>>()

    override suspend fun launch() {
        val viewRoleId = randomId("view-role")
        val message = channel.sendMessage(MessageCreate {
            content = "Please click the button below to view your role"
            actionRow(
                primary(viewRoleId, "View role")
            )
        }).await()
        messageId = message.idLong
        logger.setContext(this, "OneNightWerewolfSession:${messageId}")
        bindButton(viewRoleId, function = this::onViewRole)
        coroutineScope {
            repeat(3) { repetition ->
                val delay = when (repetition) {
                    0 -> 30.seconds
                    1 -> 60.seconds
                    else -> 90.seconds
                }
                startJobs += async {
                    delay(delay)
                    val unreadyPlayers = getUnreadyPlayers()
                    if (unreadyPlayers.isNotEmpty()) {
                        channel.sendMessage(MessageCreate {
                            val missing = unreadyPlayers.oxfordAnd { it.member().asMention }
                            content = when (repetition) {
                                0 -> "The following players have yet to view their role: $missing"
                                1 -> "The following players must view their role in 30 seconds. Otherwise the game will abort: $missing"
                                else -> "The game has been aborted..."
                            }
                            if (repetition < 2)
                                actionRow(primary(viewRoleId, "View role"))
                            else
                                cleanup()
                        }).submit().some()
                    } else {
                        None
                    }
                }
            }
        }
    }


    private fun getTeamForRole(role: OneNightWerewolfRole): OneNightWerewolfTeam {
        return teams.find { team -> team.roleInTeamByDefault(role) }!!
    }

    private fun getUnreadyPlayers(): List<OneNightWerewolfPlayer> {
        val unreadyPlayers = players.toMutableList()
        for (action in actions.filterIsInstance<StartAction>()) {
            unreadyPlayers.remove(action.player)
        }
        return unreadyPlayers
    }

    private suspend fun onViewRole(event: ButtonInteractionEvent) {
        val player = players.find { it.userId == event.member!!.idLong }
        if (player != null) {
            logger.info(this, "${event.member!!.effectiveName} viewed their role as ${player.startingRole.name}")
            if (actions.any { it is StartAction && it.player == player }) {
                logger.info(this, "${event.member!!.effectiveName} already view their role. Not adding StartAction...")
            } else {
                actions += StartAction(player)
            }
            event.reply("Your role is **${player.startingRole.name}**").setEphemeral(true).queue()
        } else {
            event.reply("You are not a player in this game.").setEphemeral(true).queue()
        }
        if (getUnreadyPlayers().isEmpty()) {
            for (startJob in startJobs) {
                if (!startJob.isCancelled) {
                    startJob.cancel()
                } else {
                    val option = startJob.await()
                    option.tap { it.await().editMessageComponents(listOf()).queue() }
                }
            }
            channel.sendMessage("Night is beginning...").queue()
            actions.sortBy { (it as StartAction).player.number }
            startNight()
        }
    }

    private suspend fun startNight() {
        logger.info(this, "Night Started")
        playersToWake = players
            .filter { it.startingRole.wakeOrder != null }
            .sortedBy { it.startingRole.wakeOrder }
            .toMutableList()
        logger.info(this,
            buildString {
                append("Players to wake during the night: ")
                append(playersToWake.joinToString(", ") { it.displayNameAndStartingCard(true) })
            }
        )
        while (playersToWake.isNotEmpty()) {
            val player = playersToWake.removeFirst()
            logger.info(this, "Player ${player.displayName()} is waking up as ${player.startingRole.name}")
            val action = player.startingRole.wakeUp(this, player, nightTimer)
            for (nightAction in action) {
                logger.info(this, nightAction.log)
            }
            actions.addAll(action)
            delay(Random.nextInt(1, 4).seconds)
        }
        channel.sendMessage("The sun is rising; All players wake up! You have ${dayTimer.toReadableString()} to discuss")
            .queue()
        delay(dayTimer - 1.minutes)
        channel.sendMessage("You have 1 minute left!").queue()
        delay(1.minutes)
        val voteId = randomId("final-vote")
        val voteMessage =
            channel.sendMessage(MessageCreate(content = "Times up! It's time to vote for who dies! You have ${voteTimer.toReadableString()}") {
                actionRow(Button.success(voteId, "Vote").withEmoji(Emoji.fromUnicode("\uD83D\uDC49")))
            }).await()
        for (player in players) {
            if (player.member().voiceState?.isMuted != true)
                player.member().mute(true).queue()
        }
        val votes = mutableMapOf<OneNightWerewolfPlayer?, MutableList<OneNightWerewolfPlayer>>()
            .withDefault {
                mutableListOf()
            }
        coroutineScope {
            for (player in players) launch {
                awaitButtonPress(voteId, player.userId, 15.seconds).getOrElse {
                    votes.getOrPut(null) { mutableListOf() } += player
                    return@launch
                }
                val (event, selections) = selectOtherPlayer(
                    player,
                    "Please select who you wish to die within ${voteTimer.toReadableString()}",
                    voteTimer
                ).getOrElse {
                    votes.getOrPut(null) { mutableListOf() } += player
                    return@launch
                }
                val selection = selections.single()
                event.editMessage("You have voted ${selection.displayName()} to die").setReplace(true).queue()
                votes[selection]!! += player
            }
        }
        voteMessage.editMessage("Voting is closed!").setReplace(true).queue()
        val voteList = votes.entries.sortedByDescending { entry -> entry.value.size }.toList()
        channel.sendMessage("")
    }


}

