package com.github.breadmoirai.discordtabletop.core.games.mafia

import com.github.breadmoirai.discordtabletop.core.games.Player
import com.github.breadmoirai.discordtabletop.discord.emoji
import net.dv8tion.jda.api.entities.Member

class MafiaPlayer(
    member: Member,
    channelId: Long,
    number: Int,
    var alignment: Alignment
) : Player(member.idLong, channelId, member.guild.idLong, number.emoji) {
    var isAlive: Boolean = true
    lateinit var role: Role

}