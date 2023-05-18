package com.github.breadmoirai.discordtabletop.core

import arrow.core.Option
import com.github.breadmoirai.discordtabletop.logging.ContextAwareLogger
import com.github.breadmoirai.discordtabletop.reactive.Event
import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.onButton
import dev.minn.jda.ktx.events.onStringSelect
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import kotlin.random.Random
import kotlin.random.nextLong
import kotlin.time.Duration

interface InteractableSession {
    val logger: ContextAwareLogger
    val inactivityLimit: Duration
    val trackedUsers: List<Long>
    val jda: JDA
    val channel: TextChannel
    val onCancel: Event<Long>
    var lastOriginalInteraction: IReplyCallback
    var lastInteraction: IReplyCallback
    var messageId: Long
    val listeners: MutableList<CoroutineEventListener>
    val hooks: MutableMap<Long, IReplyCallback>
    val hookRequests: MutableMap<Long, CompletableDeferred<Option<IReplyCallback>>>

    companion object {

        /**
         * Creates a random id with supplied [prefix] in the format `$[prefix]:$randomPositiveLong`
         *
         * @param prefix
         * @return a random component id
         */
        fun randomId(prefix: String): String {
            return "$prefix:${Random.nextLong(1..Long.MAX_VALUE)}"
        }
    }

    suspend fun launch()

    suspend fun requestInteraction(
        members: List<Long>,
        timeout: Duration,
        useMention: Boolean
    ): List<Pair<Long, Deferred<Option<IReplyCallback>>>>

    suspend fun awaitInteraction(member: Long, timeout: Duration, useMention: Boolean): Option<IReplyCallback>

    suspend fun onRefreshHook(listener: CoroutineEventListener, event: ButtonInteractionEvent)

    fun bindButton(
        componentId: String,
        function: suspend (CoroutineEventListener, ButtonInteractionEvent) -> Unit
    ) {
        listeners += jda.onButton(componentId) { event ->
            if (event.idLong == messageId) {
                lastOriginalInteraction = event
            }
            lastInteraction = event
            val userId = event.user.idLong
            if (userId in trackedUsers || trackedUsers.isEmpty()) {
                hooks[userId] = event
            }
            function(this, event)
        }
    }

    fun bindButton(
        componentId: String,
        function: suspend (ButtonInteractionEvent) -> Unit
    ) = bindButton(componentId) { _, event -> function(event) }

    fun bindStringSelect(
        componentId: String,
        function: suspend (CoroutineEventListener, StringSelectInteractionEvent) -> Unit
    ) {
        listeners += jda.onStringSelect(componentId) { event ->
            if (event.idLong == messageId) {
                lastOriginalInteraction = event
            }
            lastInteraction = event
            val userId = event.user.idLong
            if (userId in trackedUsers || trackedUsers.isEmpty()) {
                hooks[userId] = event
            }
            function(this, event)
        }
    }

    fun bindStringSelect(
        componentId: String,
        function: suspend (StringSelectInteractionEvent) -> Unit
    ) = bindStringSelect(componentId) { _, event -> function(event) }

    suspend fun cleanup()
}
