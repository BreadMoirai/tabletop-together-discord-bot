package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf

import com.github.breadmoirai.discordtabletop.core.games.Player
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.OneNightWerewolfRole
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base.Doppleganger
import com.github.breadmoirai.discordtabletop.discord.emoji

open class ONWPlayer(
    userId: Long,
    channelId: Long,
    guildId: Long,
    val session: ONWSession,
    val number: Int,
    val startingRole: OneNightWerewolfRole,
    var currentRole: OneNightWerewolfRole,
    var team: ONWTeam
) : Player(userId, channelId, guildId, number.emoji) {

    fun swapTeams(other: ONWTeam) {
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

    open fun displayNameAndCards(withNumber: Boolean): String {
        return if (startingRole != currentRole)
            buildString {
                append(displayName())
                append(" (")
                append(startingRole.displayRole(withNumber))
                if (startingRole is Doppleganger && withNumber) {
                    append("<")
                    append(startingRole.copiedRole.name)
                    append(">")
                }
                append(") -> [")
                append(currentRole.displayRole(withNumber))
                append("]")
            }
        else
            displayNameAndCard(withNumber)
    }

    open fun displayMentionAndStartingCard(withNumber: Boolean): String {
        return "${displayMention()} (${startingRole.displayRole(withNumber)})"
    }

    open fun displayMentionAndCard(withNumber: Boolean): String {
        return "${displayMention()} [${currentRole.displayRole(withNumber)}]"
    }

    open fun displayMentionAndCards(withNumber: Boolean): String {
        return if (startingRole != currentRole)
            buildString {
                append(displayMention())
                append(" (")
                append(startingRole.displayRole(withNumber))
                append(") -> [")
                append(currentRole.displayRole(withNumber))
                append("]")
            }
        else
            displayMentionAndCard(withNumber)
    }

}



