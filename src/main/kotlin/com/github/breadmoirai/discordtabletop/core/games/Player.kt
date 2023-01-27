package com.github.breadmoirai.discordtabletop.core.games

import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class Player(
    val userId: Long,
    val channelId: Long,
    val guildId: Long,
    var emoji: Emoji
) : MemberRef, KoinComponent {
    protected val jda: JDA by inject()

    val guild: Guild
        get() {
            return jda.getGuildById(guildId)
                ?: error("Guild(${guildId}) for Player(${userId}) was not found")
        }

    val channel: TextChannel
        get() {
            return guild.getTextChannelById(channelId)
                ?: error("TextChannel(${channelId}) for Player(${userId}) in Guild(${guildId}) was not found")
        }

    suspend fun user(): User {
        return jda.retrieveUserById(userId).await()
    }

    override suspend fun member(): Member {
        return guild.retrieveMemberById(userId).await()
    }

//    val durationSinceLastInteraction: Duration
//        get() {
//            if (!::hook.isInitialized) return Duration.INFINITE
//            return java.time.Duration.between(hook.interaction.timeCreated, Instant.now()).toKotlinDuration()
//        }

    open fun displayName(): String {
        return runBlocking {
            "${emoji.formatted} ${member().effectiveName}"
        }
    }

    open suspend fun asOption(): SelectOption {
        return SelectOption.of(member().effectiveName, userId.toString())
    }

    open suspend fun matchesOption(option: SelectOption): Boolean {
        return option.value.toLongOrNull() == userId
    }

//    fun hookIsValid(): Boolean {
//        return this::hook.isInitialized and !hook.isExpired
//    }
}

