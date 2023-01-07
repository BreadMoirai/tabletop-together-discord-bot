package com.github.breadmoirai.discordtabletop.api.core.game

import com.github.breadmoirai.discordtabletop.api.discord.EmojiColor
import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class Player(
    val userId: Long, val guildId: Long, val color: EmojiColor
) : KoinComponent {
    private val jda: JDA by inject()

    val user: User
        get() = runBlocking { user() }

    val member: Member
        get() = runBlocking { member() }

    suspend fun user(): User {
        return jda.retrieveUserById(userId).await()
    }

    suspend fun member(): Member {
        return jda.getGuildById(guildId)!!.retrieveMemberById(userId).await()
    }

    companion object {
        fun from(member: Member, color: EmojiColor): Player {
            return Player(member.idLong, member.guild.idLong, color)
        }
    }
}

