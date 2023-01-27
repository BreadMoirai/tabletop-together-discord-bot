package com.github.breadmoirai.discordtabletop.core

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.some
import com.github.breadmoirai.discordtabletop.core.games.MemberRef
import com.github.breadmoirai.discordtabletop.util.toReadableString
import dev.minn.jda.ktx.events.listener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import kotlin.time.Duration


/**
 * Wait for a [GenericComponentInteractionCreateEvent] event that matches the provided [componentId]
 *
 * @param T the type of [GenericComponentInteractionCreateEvent] to retrieve
 * @param componentId the id of the component to match
 * @param expiration if the [expiration] is non-null and expires, the return value [Deferred] is completed with [None]
 * @receiver [JDA]
 * @return [Option]<[T]> that is [Some]<[T]> if the event is found, else [None] if timeout.
 * If an event that matches the criteria provided does not occur, the [Deferred] will never complete.
 */
suspend inline fun <reified T : GenericComponentInteractionCreateEvent, K : MemberRef> InteractableSession<K>.awaitComponentEvent(
    componentId: String,
    expiration: Duration,
): Option<T> {
    return awaitEvent(expiration) { event ->
        event.componentId == componentId
    }
}

/**
 * Wait for a [GenericComponentInteractionCreateEvent] event that matches the provided [componentId] and [userId]
 *
 * @param T the type of [GenericComponentInteractionCreateEvent] to retrieve
 * @param componentId the id of the component to match
 * @param userId the id of the user to match
 * @param expiration if the [expiration] is non-null and expires, the return value [Deferred] is completed with [None]
 * @param onFailure if the userId does not match, this Consumer is called
 * @receiver [JDA]
 * @return [Option]<[T]> that is [Some]<[T]> if the event is found, else [None] if timeout.
 * If an event that matches the criteria provided does not occur, the [Deferred] will never complete.
 */
suspend inline fun <reified T : GenericComponentInteractionCreateEvent, K : MemberRef> InteractableSession<K>.awaitComponentEvent(
    componentId: String,
    userId: Long,
    expiration: Duration,
    crossinline onFailure: suspend (T) -> Unit
): Option<T> {
    return awaitEvent(expiration) { event ->
        if (trackedUsers.isEmpty() || event.user.idLong in trackedUsers) {
            hooks[event.user.idLong] = event
        }
        if (event.messageIdLong == messageId) {
            lastOriginalInteraction = event
        }
        lastInteraction = event
        if (event.componentId != componentId) {
            false
        } else if (userId != event.user.idLong) {
            onFailure(event)
            false
        } else true
    }
}

/**
 * Waits for a single event that satisfies [predicate].
 * If [expiration] is not defined, the [Deferred] may never complete.
 * If [expiration] expires, [Deferred] completes with [None]
 *
 * @param T type of event to wait for
 * @param expiration if this duration passes without receiving the event, completes with [None]
 * @param predicate event must pass this predicate.
 * @return [Deferred]<[Option]<[T]>> which contains [Some]<[T]> if an event was successfully received,
 * otherwise [None] if [expiration] was hit
 */
suspend inline fun <reified T : GenericEvent, K : MemberRef> InteractableSession<K>.awaitEvent(
    expiration: Duration,
    crossinline predicate: suspend (T) -> Boolean
): Option<T> {
    val id = InteractableSession.randomId(T::class.simpleName!!.replace("Event", "Listener"))
    val context = id as Any
    logger.setContext(context, id, this)
    val result = CompletableDeferred<Option<T>>()
    logger.info(context, "Adding event listener for ${T::class.simpleName}")
    val listener = jda.listener<T> { event ->
        logger.setContext(event, event.toString(), context)
        logger.info(event, "Checking $event against predicate")
        if (predicate(event)) {
            logger.info(event, "Predicate passed, removing event listener")
            result.complete(Some(event))
            this.cancel()
        } else {
            logger.info(event, "Predicate failed")
        }
    }
    logger.info(context, "Adding timeout listener, waiting for ${expiration.toReadableString()}")
    coroutineScope {
        val job = launch {
            delay(expiration)
            if (result.isActive) {
                logger.info(context, "Timeout hit, completing result with None")
                result.complete(None)
            }
        }
        launch {
            result.await()
            job.cancel()
        }
    }
    return result.await()
}

/**
 * Wait for a [ButtonInteractionEvent] event that matches the provided [componentId] and [userId]
 *
 * @param componentId the id of the component to match
 * @param userId the id of the user to match
 * @param expiration if the [expiration] is non-null and expires, the return value [Deferred] is completed with [None]
 * @param onFailure if the userId does not match, this Consumer is called
 * @return [Deferred]<[Option]<[ButtonInteractionEvent]>> that contains [Some]<[ButtonInteractionEvent]> if the event is found, else [None] if timeout.
 * If an event that matches the criteria provided does not occur, the [Deferred] will never complete.
 */
suspend fun <K : MemberRef> InteractableSession<K>.awaitButtonPress(
    componentId: String,
    userId: Long,
    expiration: Duration,
    onFailure: suspend (ButtonInteractionEvent) -> Unit = { it.deferEdit().queue() }
): Option<ButtonInteractionEvent> {
    return awaitEvent(expiration) { event ->
        if (event.componentId != componentId) {
            logger.info(
                event,
                "Predicate failed against component id: needed $componentId but found ${event.componentId}"
            )
            false
        } else if (event.user.idLong != userId) {
            logger.info(
                event,
                "Predicate failed against user id: needed $userId but found ${event.user.idLong}; invoking onFailure"
            )
            onFailure(event)
            false
        } else {
            true
        }
    }
}

/**
 * Wait for a [ButtonInteractionEvent] event that matches any of the provided [componentId] and [userId]
 *
 * @param componentId the id of the component to match
 * @param userId the id of the user to match
 * @param expiration if the [expiration] is non-null and expires, the return value [Deferred] is completed with [None]
 * @param onFailure if the userId does not match, this Consumer is called
 * @return [Deferred]<[Option]<[ButtonInteractionEvent]>> that contains [Some]<[ButtonInteractionEvent]> if the event is found, else [None] if timeout.
 * If an event that matches the criteria provided does not occur, the [Deferred] will never complete.
 */
suspend fun <K : MemberRef> InteractableSession<K>.awaitButtonPress(
    componentIds: List<String>,
    userId: Long,
    expiration: Duration,
    onFailure: suspend (ButtonInteractionEvent) -> Unit = { it.deferEdit().queue() }
): Option<ButtonInteractionEvent> {
    return coroutineScope {
        val channel = Channel<ButtonInteractionEvent>()
        val jobs = mutableListOf<Job>()
        for (componentId in componentIds) {
            jobs += launch {
                awaitButtonPress(componentId, userId, expiration, onFailure).tap { event ->
                    logger.info(event, "Sending button press to channel")
                    channel.send(event)
                    logger.info(event, "Closing channel")
                    channel.close()
                }
            }
        }
        val returnVal = channel.receiveCatching().getOrNull()?.some() ?: None
        jobs.forEach { it.cancel() }
        returnVal
    }
}

/**
 * Wait for a [StringSelectInteractionEvent] event that matches the provided [componentId] and [userId]
 *
 * @param componentId the id of the component to match
 * @param userId the id of the user to match
 * @param expiration if the [expiration] is non-null and expires, the return value [Deferred] is completed with [None]
 * @param onFailure if the userId does not match, this Consumer is called
 * @return [Option]<[StringSelectInteractionEvent]> that is [Some]<[StringSelectInteractionEvent]> if the event is found, else [None] if timeout.
 */
suspend fun <K : MemberRef> InteractableSession<K>.awaitStringSelect(
    componentId: String,
    userId: Long,
    expiration: Duration,
    onFailure: suspend (StringSelectInteractionEvent) -> Unit = { it.deferEdit().queue() }
): Option<StringSelectInteractionEvent> {
    return awaitEvent(expiration) { event ->
        if (event.componentId != componentId) {
            false
        } else if (userId != event.user.idLong) {
            onFailure(event)
            false
        } else true
    }
}

/**
 * Wait for a [StringSelectInteractionEvent] event that matches the provided [componentId]
 *
 * @param componentId the id of the component to match
 * @param expiration if the [expiration] is non-null and expires, the return value [Deferred] is completed with [None]
 * @return [Option]<[StringSelectInteractionEvent]> that is [Some]<[StringSelectInteractionEvent]> if the event is found, else [None] if timeout.
 */
suspend fun <K : MemberRef> InteractableSession<K>.awaitStringSelect(
    componentId: String,
    expiration: Duration,
): Option<StringSelectInteractionEvent> {
    return awaitEvent(expiration) { event ->
        event.componentId == componentId
    }
}