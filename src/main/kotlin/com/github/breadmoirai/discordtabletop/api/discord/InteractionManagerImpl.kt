package com.github.breadmoirai.discordtabletop.api.discord

import com.github.breadmoirai.discordtabletop.api.jda.GuildButtonInteractionEvent
import com.github.breadmoirai.discordtabletop.api.jda.GuildEntitySelectInteractionEvent
import com.github.breadmoirai.discordtabletop.api.jda.GuildStringSelectInteractionEvent
import com.github.breadmoirai.discordtabletop.api.logging.logger
import com.github.breadmoirai.discordtabletop.api.reactive.Cancellable
import com.github.breadmoirai.discordtabletop.api.reactive.impl.CancellableConsumer
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import org.koin.core.component.KoinComponent
import java.util.concurrent.ConcurrentHashMap

class InteractionManagerImpl(jda: JDA) : InteractionManager, KoinComponent {
    private val logger by logger<InteractionManager>()

    private val buttonHandlers: MutableMap<String, CancellableConsumer<GuildButtonInteractionEvent>> =
        ConcurrentHashMap()
    private val buttonMessageHandlers: MutableMap<ComponentKey, CancellableConsumer<GuildButtonInteractionEvent>> =
        ConcurrentHashMap()
    private val stringSelectMenuHandlers: MutableMap<String, CancellableConsumer<GuildStringSelectInteractionEvent>> =
        ConcurrentHashMap()
    private val stringSelectMenuMessageHandlers: MutableMap<ComponentKey, CancellableConsumer<GuildStringSelectInteractionEvent>> =
        ConcurrentHashMap()
    private val entitySelectMenuHandlers: MutableMap<String, CancellableConsumer<GuildEntitySelectInteractionEvent>> =
        ConcurrentHashMap()
    private val entitySelectMenuMessageHandlers: MutableMap<ComponentKey, CancellableConsumer<GuildEntitySelectInteractionEvent>> =
        ConcurrentHashMap()

    init {
        jda.listener<ButtonInteractionEvent> { e ->
            val event = GuildButtonInteractionEvent(e)
            logger.setContext(event, "(${event.id})ButtonInteractionEvent{${event.componentId}}")
            logger.info(event, "${event.member.effectiveName} pressed Button")
            buttonHandlers[event.componentId]?.accept(event)
            buttonMessageHandlers[ComponentKey(event.componentId, event.messageIdLong)]?.accept(event)
            buttonMessageHandlers[ComponentKey(event.componentId, event.hook.interaction.idLong)]?.accept(event)
        }
        jda.listener<StringSelectInteractionEvent> { e ->
            val event = GuildStringSelectInteractionEvent(e)
            logger.setContext(event, "(${event.id})StringSelectInteractionEvent{${event.componentId}}")
            logger.info(event, "${event.member.effectiveName} selected String(s) ${event.values}")
            stringSelectMenuHandlers[event.componentId]?.accept(event)
            stringSelectMenuMessageHandlers[ComponentKey(event.componentId, event.messageIdLong)]?.accept(event)
            stringSelectMenuMessageHandlers[ComponentKey(event.componentId, event.hook.interaction.idLong)]?.accept(event)
        }
        jda.listener<EntitySelectInteractionEvent> { e ->
            val event = GuildEntitySelectInteractionEvent(e)
            logger.setContext(event, "(${event.id})EntitySelectInteractionEvent{${event.componentId}}")
            logger.info(event, "${event.member.effectiveName} selected Entity(ies) ${event.values}")
            entitySelectMenuHandlers[event.componentId]?.accept(event)
            entitySelectMenuMessageHandlers[ComponentKey(event.componentId, event.messageIdLong)]?.accept(event)
            entitySelectMenuMessageHandlers[ComponentKey(event.componentId, event.hook.interaction.idLong)]?.accept(event)
        }
    }

    override fun addButtonHandler(componentId: String, onButton: suspend (Cancellable, GuildButtonInteractionEvent) -> Unit): Cancellable {
        val job = CancellableConsumer(onButton) {
            buttonHandlers.remove(componentId)
        }
        buttonHandlers[componentId] = job
        return job
    }

    override fun addButtonHandler(
        componentId: String,
        messageId: Long,
        onButton: suspend (Cancellable, GuildButtonInteractionEvent) -> Unit
    ): Cancellable {
        val key = ComponentKey(componentId, messageId)
        val job = CancellableConsumer(onButton) {
            buttonMessageHandlers.remove(key)
        }
        buttonMessageHandlers[key] = job
        return job
    }

    override fun addStringSelectMenuHandler(
        componentId: String,
        onSelect: suspend (Cancellable, GuildStringSelectInteractionEvent) -> Unit
    ): Cancellable {
        val job = CancellableConsumer(onSelect) {
            stringSelectMenuHandlers.remove(componentId)
        }
        stringSelectMenuHandlers[componentId] = job
        return job
    }

    override fun addStringSelectMenuHandler(
        componentId: String,
        messageId: Long,
        onSelect: suspend (Cancellable, GuildStringSelectInteractionEvent) -> Unit
    ): Cancellable {
        val key = ComponentKey(componentId, messageId)
        val job = CancellableConsumer(onSelect) {
            stringSelectMenuMessageHandlers.remove(key)
        }
        stringSelectMenuMessageHandlers[key] = job
        return job
    }


    override fun addEntitySelectMenuHandler(
        componentId: String,
        onSelect: suspend (Cancellable, GuildEntitySelectInteractionEvent) -> Unit
    ): Cancellable {
        val job = CancellableConsumer(onSelect) {
            entitySelectMenuHandlers.remove(componentId)
        }
        entitySelectMenuHandlers[componentId] = job
        return job
    }


    override fun addEntitySelectMenuHandler(
        componentId: String,
        messageId: Long,
        onSelect: suspend (Cancellable, GuildEntitySelectInteractionEvent) -> Unit
    ): Cancellable {
        val key = ComponentKey(componentId, messageId)
        val job = CancellableConsumer(onSelect) {
            entitySelectMenuMessageHandlers.remove(key)
        }
        entitySelectMenuMessageHandlers[key] = job
        return job
    }


}