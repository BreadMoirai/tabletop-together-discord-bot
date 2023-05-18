package com.github.breadmoirai.discordtabletop.core

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import com.github.breadmoirai.discordtabletop.core.InteractableSession.Companion.randomId
import com.github.breadmoirai.discordtabletop.core.games.MemberRef
import com.github.breadmoirai.discordtabletop.logging.logger
import com.github.breadmoirai.discordtabletop.reactive.Event
import com.github.breadmoirai.discordtabletop.reactive.event
import com.github.breadmoirai.discordtabletop.util.oxfordAnd
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.interactions.components.primary
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration
import java.time.Duration as JavaDuration

abstract class BaseInteractableSession(
    override val inactivityLimit: Duration,
    override val trackedUsers: List<Long>,
    initialInteraction: IReplyCallback
) : KoinComponent, InteractableSession {
    final override var lastInteraction: IReplyCallback = initialInteraction
    final override var lastOriginalInteraction: IReplyCallback = initialInteraction
    override val jda by inject<JDA>()
    override val channel = lastInteraction.messageChannel as TextChannel
    override val onCancel: Event<Long> = event()
    override val hooks = mutableMapOf<Long, IReplyCallback>()
    override val hookRequests = mutableMapOf<Long, CompletableDeferred<Option<IReplyCallback>>>()
    override var messageId by Delegates.notNull<Long>()
    override val listeners: MutableList<CoroutineEventListener> = Collections.synchronizedList(mutableListOf())
    override val logger by logger()
    private val mutex = Mutex()
    private val refreshHookId = randomId("refresh-interaction-hook")


    override suspend fun launch() {
        bindButton(refreshHookId, ::onRefreshHook)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private val inactivityJob: Job = GlobalScope.launch {
        val buffer = 5.seconds
        delay(inactivityLimit)
        while (true) {
            val timeSinceLastInteraction =
                JavaDuration.between(lastInteraction.timeCreated, OffsetDateTime.now()).toKotlinDuration()
            if (timeSinceLastInteraction < inactivityLimit) {
                delay(inactivityLimit - timeSinceLastInteraction + buffer)
            } else {
                onSessionTimeout()
                return@launch
            }
        }
    }

    // use T with (T) -> Member
    // add override for Player?
    override suspend fun requestInteraction(
        members: List<Long>,
        timeout: Duration,
        useMention: Boolean
    ): List<Pair<Long, Deferred<Option<IReplyCallback>>>> {
        assert(members.isNotEmpty()) { "members may not be empty" }
        logger.info(this, "Interaction requested for members: ${members.oxfordAnd { "$it" }}")
        for (member in members) {
            val prior = hooks[member]
            when {
                prior == null -> logger.info(this, "No prior interaction found for $member")
                prior.hook.isExpired -> logger.info(this, "Expired interaction found for $member")
                else -> logger.info(this, "Valid interaction found for $member")
            }
        }
        if (members.all { hooks[it]?.hook?.isExpired == false }) {
            logger.info(
                this,
                "Valid interactions found for all members: ${members.oxfordAnd { "$it" }}"
            )
            return members.map { it to CompletableDeferred(hooks[it]!!.some()) }
        }
        logger.info(
            this,
            "Creating interaction requests for members: ${members.oxfordAnd { "$it" }}"
        )
        val requests = members.map { member ->
            val deferred = CompletableDeferred<Option<IReplyCallback>>()
            if (hookRequests[member] != null) {
                error("Interaction hook already requested for member ${member}(${member})")
            }
            hookRequests[member] = deferred
            member to deferred
        }
        coroutineScope {
            for ((_, def) in requests) {
                launch {
                    val option = def.await()
                    if (option is None) return@launch
                    val remaining = requests.filter { (_, r) -> !r.isCompleted }
                    if (remaining.all { (m, _) -> hooks[m]?.hook?.isExpired == false }) {
                        remaining.forEach { (m, r) -> r.complete(hooks[m]!!.some()) }
                    }
                }
            }
        }
        channel.sendMessage(MessageCreate {
            content =
                if (useMention) "${members.oxfordAnd { member -> "<@$member>" }}; Please press the button below to continue"
                else "All players, please press the button below to continue"
            actionRow(primary(refreshHookId, "Press here to continue..."))
        }).queue()
        coroutineScope {
            launch {
                delay(timeout)
                requests.forEach { (m, r) ->
                    r.complete(None)
                    hookRequests.remove(m, r)
                }
            }
        }
        return requests
    }

    override suspend fun awaitInteraction(
        member: Long,
        timeout: Duration,
        useMention: Boolean
    ): Option<IReplyCallback> {
        logger.info(this, "Interaction requested for member: $member")
        val prior = hooks[member]
        when {
            prior == null -> {
                logger.info(this, "No prior interaction found for $member}")
            }

            prior.hook.isExpired -> {
                logger.info(this, "Expired interaction found for $member")
            }

            else -> {
                logger.info(this, "Valid interaction found for $member")
                return prior.some()
            }
        }
        logger.info(
            this,
            "Creating interaction requests for member: $member"
        )
        val deferred = CompletableDeferred<Option<IReplyCallback>>()
        if (hookRequests[member] != null) {
            error("Interaction hook already requested for member $member")
        }
        hookRequests[member] = deferred
        channel.sendMessage(MessageCreate {
            content =
                if (useMention) "<@$member>; Please press the button below to continue"
                else "All players, please press the button below to continue"
            actionRow(primary(refreshHookId, "Press here to continue..."))
        }).queue()
        coroutineScope {
            launch {
                delay(timeout)
                deferred.complete(None)
            }
        }
        return deferred.await()
    }

    override suspend fun onRefreshHook(listener: CoroutineEventListener, event: ButtonInteractionEvent) {
        event.deferEdit().queue()
        hookRequests.remove(event.user.idLong)?.complete(event.some())
    }

    override suspend fun cleanup() {
        mutex.withLock {
            if (listeners.isEmpty()) return
            listeners.forEach { it.cancel() }
            listeners.clear()
            inactivityJob.cancel()
        }
    }

    protected open suspend fun onSessionTimeout() {
        cleanup()
//        if (!lastInteraction.hook.isExpired)
//            lastInteraction.hook.edit(cancelledGame).queue()
//        else
//            channel.editMessageById(
//                messageId,
//                cancelledGame
//            ).queue()
        onCancel.invokeEvent(messageId)
    }


}