package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf

import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.OneNightWerewolfRole
import net.dv8tion.jda.api.interactions.components.selections.SelectOption

class ONWCenterCard(
    number: Int,
    session: ONWSession,
    role: OneNightWerewolfRole,
    team: ONWTeam
) : ONWPlayer(
    number * -1L,
    session.channel.idLong,
    session.channel.guild.idLong,
    session,
    number,
    role,
    role,
    team
) {

    override fun displayName(): String {
        return emoji.formatted
    }

    override fun asOption(): SelectOption {
        return SelectOption.of(" ", userId.toString()).withEmoji(emoji)
    }

    override fun matchesOption(option: SelectOption): Boolean {
        return super.matchesOption(option)
    }
}