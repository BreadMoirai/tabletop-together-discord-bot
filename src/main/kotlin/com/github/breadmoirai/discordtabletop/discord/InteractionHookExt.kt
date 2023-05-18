package com.github.breadmoirai.discordtabletop.discord

import arrow.core.None
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.requests.restaction.MessageEditAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction

fun InteractionHook.editOriginalComponents(vararg component: ActionComponent): WebhookMessageEditAction<Message> {
    return this.editOriginalComponents(ActionRow.of(component.toList()))
}

fun ButtonInteractionEvent.editComponents(vararg component: ActionComponent): MessageEditCallbackAction {
    return this.editComponents(ActionRow.of(component.toList()))
}
fun Message.editMessageComponents(vararg component: ActionComponent): MessageEditAction {
    return this.editMessageComponents(ActionRow.of(component.toList()))
}

