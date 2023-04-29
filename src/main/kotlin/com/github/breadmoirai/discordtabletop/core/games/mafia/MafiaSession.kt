package com.github.breadmoirai.discordtabletop.core.games.mafia

import com.github.breadmoirai.discordtabletop.core.InteractableSession.Companion.randomId
import com.github.breadmoirai.discordtabletop.core.games.GameSession
import com.github.breadmoirai.discordtabletop.discord.bold
import com.github.breadmoirai.discordtabletop.reactive.event
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.SelectOption
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.utils.TimeFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration


class MafiaSession(
    inactivityLimit: Duration,
    trackedUsers: List<Long>,
    initialInteraction: IReplyCallback,
    override val players: List<MafiaPlayer>,
    val readyDuration: Duration = 1.minutes,
    val nightStart: Boolean,
    var dayDuration: Duration = 5.minutes,
    val trialsEnabled: Boolean,
    var nightDuration: Duration = 2.minutes,
    val nightMeetings: List<MafiaTag>
) : GameSession<MafiaPlayer>(inactivityLimit, trackedUsers, initialInteraction) {
    var currentDay: Int = if (nightStart) 0 else 1
    val onDayStart = event<MafiaSession>()
    val onNightStart = event<MafiaSession>()
    val onAction = event<ActionEvent>()
    private val condemnSelectId = randomId("mafia-condemn")
    private lateinit var currentDayStatus: Message
    private val _privateThreads = mutableMapOf<MafiaPlayer, ThreadChannel>()
    val privateThreads: Map<MafiaPlayer, ThreadChannel>
        get() = _privateThreads
    private lateinit var _logThread: ThreadChannel
    private val logThread: ThreadChannel
        get() = _logThread

    /**
     * list of condemnVotes organized as **`source to target`**
     */
    val condemnVotes = mutableListOf<Pair<MafiaPlayer, MafiaPlayer>>()
    val voteCountMap: Map<MafiaPlayer, Int>
        get() = condemnVotes.groupBy({ (_, target) -> target }, { (source, _) -> source })
            .mapValues { (_, votedBy) -> votedBy.size }

    override suspend fun launch() {
        super.launch()
        coroutineScope {
            _logThread = channel.createThreadChannel(
                buildString {
                    append("Mafia Game Log - ")
                    append(LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)))
                }, true
            ).await()
            players.forEach { player ->
                launch {
                    val privateThread =
                        channel.createThreadChannel("${player.emoji} - ${player.name}'s Game Log", true).await()
                    _privateThreads[player] = privateThread
                    privateThread.manager.setInvitable(false).await()
                    privateThread.addThreadMemberById(player.userId).await()
                    privateThread.sendMessage("You are a ${player.selfRole.bold()}").queue()
                }
            }

        }
        if (nightStart) {
            startNight()
        } else {
            startDay()
        }
        bindStringSelect(condemnSelectId, ::onCondemn)
    }

    private suspend fun onCondemn(event: StringSelectInteractionEvent) {
        // guard clauses
        val player = players.find { event.user.idLong == it.userId }
        if (player == null) {
            event.reply("You cannot condemn if you are not in the game. Please do not abuse the bot.")
                .setEphemeral(true)
                .queue()
            return
        }
        if (!player.isAlive) {
            event.reply("You cannot condemn if you are dead. Please do not abuse me.")
                .setEphemeral(true)
                .queue()
            return
        }

        // display vote message
        val targetOption = event.selectedOptions.single()
        val targetPlayer = players.find { it.matchesOption(targetOption) }
        if (targetPlayer == null) {
            event.channel.sendMessage("${player.displayName().bold()} voted to condemn no one").queue()
            return
        }
        event.channel.sendMessage(
            "${player.displayName().bold()} voted to condemn ${
                targetPlayer.displayName().bold()
            }"
        ).queue()

        // update condemnVotes
        val removedVote = condemnVotes.find { (source, _) -> source == player }
        if (removedVote != null) {
            condemnVotes.remove(removedVote)
        }
        condemnVotes.add(player to targetPlayer)

        // update nicknames with vote count
        targetPlayer.member()
            .modifyNickname("${targetPlayer.displayNickname()} (${voteCountMap[targetPlayer]!!})")
            .queue()
        if (removedVote != null) {
            val removedTarget = removedVote.second
            if (voteCountMap.containsKey(removedTarget))
                removedTarget.member()
                    .modifyNickname("${targetPlayer.displayNickname()} (${voteCountMap[targetPlayer]!!})")
                    .queue()
            else
                removedTarget.member()
                    .modifyNickname(targetPlayer.displayNickname())
                    .queue()
        }

        // update day summary
        event.editMessage(MessageEdit {
            embed {
                title = "Day $currentDay"
                field {
                    name = "Graveyard"
                    value = players.filter { !it.isAlive }.map { it.displayName() }.joinToString("\n")
                    inline = true
                }
                field {
                    name = "The Living"
                    value = players.filter(MafiaPlayer::isAlive).map { it.displayName() }.joinToString("\n")
                    inline = true
                }
            }
        }).queue()
    }

    suspend fun sendDayStatus() {
        currentDayStatus = channel.sendMessage(MessageCreate {
            embed {
                title = "Day $currentDay"
                field {
                    name = "Graveyard"
                    value = players.filter { !it.isAlive }.map { it.displayName() }.joinToString("\n")
                    inline = true
                }
                field {
                    name = "The Living"
                    value = players.filter(MafiaPlayer::isAlive).map { it.displayName() }.joinToString("\n")
                    inline = true
                }
            }
            content = "Day $currentDay will end ${TimeFormat.RELATIVE.now().plus(5.minutes.toJavaDuration())}"
            actionRow(
                StringSelectMenu(
                    condemnSelectId,
                    placeholder = "Choose a player to Condemn",
                    options = players.filter(MafiaPlayer::isAlive).map { it.asOption() } + SelectOption(
                        "No one",
                        "no_one"
                    )
                )
            )
        }).await()
    }

    suspend fun startDay() {
        coroutineScope {
            sendDayStatus()
            launch {
                onDayStart.invokeEvent(this@MafiaSession)
            }
            delay(dayDuration)
            // remove condemn interaction
            currentDayStatus.editMessage(MessageEdit(components = listOf())).queue()
            // check if anyone has majority condemns
            val aliveCount = players.count(MafiaPlayer::isAlive)
            val majorityThreshold = (aliveCount / 2) + 1
            val conviction = voteCountMap.entries.find { (_, count) ->
                count >= majorityThreshold
            }
            if (conviction != null) {
                val (convicted, _) = conviction
                val action = CondemnAction(
                    convicted,
                    condemnVotes.filter { (_, target) -> target == convicted }.map { (condemner, _) -> condemner })
                val event = ActionEvent(this@MafiaSession, listOf(action))
                onAction.invokeEvent(event)
                event.actions.forEach { it.apply(this@MafiaSession, event) }
            }
        }

    }

    suspend fun startNight() {
        onNightStart.invokeEvent(this)
    }
}
