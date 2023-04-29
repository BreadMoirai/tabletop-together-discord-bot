package com.github.breadmoirai.discordtabletop.core.games.mafia

import com.github.breadmoirai.discordtabletop.core.games.Player
import com.github.breadmoirai.discordtabletop.discord.emoji
import dev.minn.jda.ktx.interactions.components.SelectOption
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.interactions.components.selections.SelectOption

class MafiaPlayer(
    member: Member,
    channelId: Long,
    val number: Int,
    val name: String,
    var role: MafiaRole,
    var alignment: Alignment,
    var protectionLevel: Int = 0
) : Player(member.idLong, channelId, member.guild.idLong, number.emoji) {
    var isAlive: Boolean = true
    var isRevealed: Boolean = true
    lateinit var revealedRole: String
    var selfRole: String = role.name

    override suspend fun displayName(): String {
        return if (isRevealed)
            "$emoji $name <${revealedRole}>"
        else
            "$emoji $name"
    }

    override suspend fun displayMention(): String {
        return "$emoji ${member().asMention}"
    }

    suspend fun displayNickname(): String {
        return if (isRevealed)
            "[$number] $name <${revealedRole}>"
        else
            "[$number] $name"
    }

    override suspend fun asOption(): SelectOption {
        return if (isRevealed) {
            SelectOption("$name (${revealedRole})", userId.toString(), emoji = emoji)
        } else {
            SelectOption(name, userId.toString(), emoji = emoji)
        }
    }

}