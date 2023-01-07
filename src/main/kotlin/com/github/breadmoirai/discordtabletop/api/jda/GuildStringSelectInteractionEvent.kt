package com.github.breadmoirai.discordtabletop.api.jda

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.StringSelectInteraction

class GuildStringSelectInteractionEvent(private val event: StringSelectInteractionEvent): StringSelectInteraction by event {
    init {
        if (!event.isFromGuild) {
            throw IllegalArgumentException("$event is not from a guild and cannot be adapted into a GuildButtonInteractionEvent")
        }
    }

    override fun getGuild(): Guild {
        return event.guild!!
    }

    override fun getMember(): Member {
        return event.member!!
    }
}