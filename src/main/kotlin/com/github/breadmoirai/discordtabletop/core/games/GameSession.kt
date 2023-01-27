package com.github.breadmoirai.discordtabletop.core.games

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import com.github.breadmoirai.discordtabletop.core.BaseInteractableSession
import com.github.breadmoirai.discordtabletop.core.InteractableSession
import com.github.breadmoirai.discordtabletop.core.awaitButtonPress
import com.github.breadmoirai.discordtabletop.core.awaitStringSelect
import com.github.breadmoirai.discordtabletop.util.oxfordAnd
import com.github.breadmoirai.discordtabletop.util.toReadableString
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import kotlin.time.Duration

abstract class GameSession<T : Player>(
    inactivityLimit: Duration,
    trackedUsers: List<Long>,
    initialInteraction: IReplyCallback
) : BaseInteractableSession<T>(
    inactivityLimit,
    trackedUsers,
    initialInteraction
) {

    abstract val players: List<T>

    suspend fun selectOtherPlayer(
        source: T,
        message: String,
        timeout: Duration,
        from: List<T> = players,
        count: Int = 1,
        useMention: Boolean = false
    ): Option<Pair<StringSelectInteractionEvent, List<T>>> {
        val other = from.filter { it != source }
        return selectPlayer(source, message, timeout, other, count, useMention)
    }

    suspend fun selectPlayer(
        source: T,
        message: String,
        timeout: Duration,
        from: List<T> = players,
        count: Int = 1,
        useMention: Boolean = false
    ): Option<Pair<StringSelectInteractionEvent, List<T>>> {
        assert(from.isNotEmpty()) { "from may not be empty" }
        assert(message.isNotBlank()) { "message must not be blank" }
        logger.info(
            this,
            "${source.displayName()} is selecting another player from ${from.oxfordAnd { it.displayName() }}"
        )
        val resp = awaitInteraction(source, timeout, useMention).getOrElse {
            return None
        }
        val id = InteractableSession.randomId("select-other-player")
        val msg = MessageCreate {
            content = message
            actionRow(StringSelectMenu(id) {
                maxValues = count
                minValues = count
                for (p in from) {
                    options += p.asOption()
                }
            })
        }
        val sent: Any = if (!resp.isAcknowledged) {
            resp.reply(msg).setEphemeral(true).await()
        } else {
            resp.hook.sendMessage(msg).setEphemeral(true).await()
        }
        val selection = awaitStringSelect(id, timeout).getOrElse {
            val timeoutMessage = MessageEdit(embeds = emptyList(), components = emptyList()) {
                content = "Failed to respond within ${timeout.toReadableString()}"
            }
            when (sent) {
                is InteractionHook -> sent.editOriginal(timeoutMessage).queue()
                is Message -> sent.editMessage(timeoutMessage).queue()
            }
            return None
        }
        logger.info(this, "${source.displayName()} selected ${selection.selectedOptions.oxfordAnd { it.value }}")
        val selectedPlayers = selection.selectedOptions.map { option ->
            from.find { player -> option.value.toLong() == player.userId }!!
        }
        return Some(selection to selectedPlayers)
    }

    suspend fun selectButton(
        players: List<T>,
        message: String,
        buttons: List<Button>,
        timeout: Duration,
        useMention: Boolean,
        private: Boolean
    ): List<Pair<T, Deferred<Option<ButtonInteractionEvent>>>> {
        assert(buttons.size <= 5) { "Not Implemented" }
        logger.info(
            this,
            "${players.oxfordAnd { it.displayName() }} are selecting from ${buttons.oxfordAnd { it.id!! }}"
        )
//        assert(buttons.all { it.id != null }) { "All buttons must have an id" }
        val messageData = MessageCreate {
            content = message
            actionRow(buttons)
        }
        return if (!private) {
            channel.sendMessage(messageData).queue()
            coroutineScope {
                val buttonPresses = players.map { p ->
                    async {
                        awaitButtonPress(buttons.map { it.id!! }, p.userId, timeout).tap { event ->
                            event.reply("You have selected **${event.button.label}**")
                                .setEphemeral(true)
                                .queue()
                        }
                    }
                }
                players.zip(buttonPresses)
            }
        } else {
            val interactions = requestInteraction(players, timeout, useMention)
            coroutineScope {
                interactions.map { (p, interaction) ->
                    p to async {
                        interaction.await().flatMap { interaction ->
                            logger.info(this@GameSession, "Sending select message for ${p.displayName()}")
                            interaction.hook.sendMessage(messageData).setEphemeral(true).await()
                            awaitButtonPress(buttons.map { b -> b.id!! }, p.userId, timeout).tap { event ->
                                event.reply("You have selected **${event.button.emoji?.formatted ?: ""}${event.button.label}**")
                                    .setEphemeral(true)
                                    .queue()
                            }.tapNone {
                                if (!interaction.hook.isExpired) {
                                    interaction.hook.sendMessage("You have failed to select any option")
                                        .setEphemeral(true)
                                        .queue()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun selectButton(
        player: T,
        message: String,
        buttons: List<Button>,
        timeout: Duration,
        useMention: Boolean
    ): Option<ButtonInteractionEvent> {
        assert(buttons.size <= 5) { "Not Implemented" }
        logger.info(
            this,
            "${player.displayName()} is selecting from ${buttons.oxfordAnd { it.id!! }}"
        )
        val interaction = awaitInteraction(player, timeout, useMention).getOrElse { return None }
        logger.info(this@GameSession, "Sending select message for ${player.displayName()}")
        val sent = interaction.hook.sendMessage(MessageCreate(message, components = listOf(ActionRow.of(buttons))))
            .setEphemeral(true).await()
        return awaitButtonPress(buttons.map { b -> b.id!! }, player.userId, timeout).tapNone {
            if (!interaction.hook.isExpired) {
                sent.editMessage("You have failed to select any option").setReplace(true).queue()
            }
        }
    }
}