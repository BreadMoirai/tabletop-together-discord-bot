package com.github.breadmoirai.discordtabletop.api.jda

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.CommandInteraction
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction

class GuildCommandInteractionEvent(private val event: GenericCommandInteractionEvent) : CommandInteraction by event,
    GenericEvent by event {

    init {
        if (!event.isFromGuild) {
            throw IllegalArgumentException("$event is not from a guild and cannot be adapted into a GuildCommandInteractionEvent")
        }
    }

    override fun getGuild(): Guild {
        return event.guild!!
    }

    override fun getMember(): Member {
        return event.member!!
    }

    override fun getChannel(): Channel {
        return event.channel!!
    }

    override fun getJDA(): JDA {
        return event.jda
    }

    fun getInteraction(): CommandInteraction {
        return event.interaction
    }

    override fun getCommandType(): Command.Type {
        return getInteraction().commandType
    }

    override fun getName(): String {
        return getInteraction().name
    }

    override fun getSubcommandName(): String? {
        return getInteraction().subcommandName
    }

    override fun getSubcommandGroup(): String? {
        return getInteraction().subcommandGroup
    }

    override fun getCommandIdLong(): Long {
        return getInteraction().commandIdLong
    }

    override fun isGuildCommand(): Boolean {
        return getInteraction().isGuildCommand
    }

    override fun getOptions(): List<OptionMapping> {
        return getInteraction().options
    }

    override fun getHook(): InteractionHook {
        return getInteraction().hook
    }

    override fun deferReply(): ReplyCallbackAction {
        return getInteraction().deferReply()
    }

    override fun replyModal(modal: Modal): ModalCallbackAction {
        return getInteraction().replyModal(modal)
    }
}