package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf

import com.github.breadmoirai.discordtabletop.core.games.Player
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.OneNightWerewolfRole
import com.github.breadmoirai.discordtabletop.discord.emoji

open class OneNightWerewolfPlayer(
    userId: Long,
    channelId: Long,
    guildId: Long,
    val session: OneNightWerewolfSession,
    val number: Int,
    val startingRole: OneNightWerewolfRole,
    var currentRole: OneNightWerewolfRole,
    var team: OneNightWerewolfTeam
) : Player(userId, channelId, guildId, number.emoji) {

    fun swapTeams(other: OneNightWerewolfTeam) {
        if (team == other) return
        team.players.remove(this)
        other.players.add(this)
        team = other
    }

    open fun displayNameAndStartingCard(withNumber: Boolean): String {
        return "${displayName()} (${startingRole.displayRole(withNumber)})"
    }

    open fun displayNameAndCard(withNumber: Boolean): String {
        return "${displayName()} [${currentRole.displayRole(withNumber)}]"
    }

}



