package com.github.breadmoirai.discordtabletop.core.games.mafia

import arrow.core.None
import arrow.core.Option
import arrow.core.some

sealed class ActionTarget {
    fun getTarget(session: MafiaSession, source: MafiaPlayer): List<MafiaPlayer> {
        return getTarget(session, source.some())
    }

    fun getTarget(session: MafiaSession): List<MafiaPlayer> {
        return getTarget(session, None)
    }

    abstract fun getTarget(session: MafiaSession, source: Option<MafiaPlayer>): List<MafiaPlayer>
}

class RandomTarget(
    val dead: Boolean, val alive: Boolean, val self: Boolean, val ally: Boolean, val count: Int = 1
) : ActionTarget() {
    override fun getTarget(session: MafiaSession, source: Option<MafiaPlayer>): List<MafiaPlayer> {
        return session.players.filter { player ->
            when {
                dead && !player.isAlive           -> true
                alive && player.isAlive           -> true
                self && player == source.orNull() -> true
                ally && player.alignment != Alignment.Unaffiliated && source.map(MafiaPlayer::alignment)
                    .orNull() == player.alignment -> true

                else                              -> false
            }
        }.shuffled().take(count)
    }
}

class SelectedTarget(
    val dead: Boolean, val alive: Boolean, val self: Boolean, val ally: Boolean, val count: Int = 1
) : ActionTarget() {
    override fun getTarget(session: MafiaSession, source: Option<MafiaPlayer>): List<MafiaPlayer> {
        val options = session.players.filter { player ->
            when {
                dead && !player.isAlive                               -> true
                alive && player.isAlive                               -> true
                self && player == source.orNull()                     -> true
                ally && source.map(MafiaPlayer::alignment).orNull() == player.alignment
                        && player.alignment != Alignment.Unaffiliated -> true
                else                                                  -> false
            }
        }
        // TODO Request User Selection
        return mutableListOf()
    }
}

class RoleTarget(
    val list: List<MafiaRole>,
    val self: Boolean,
    val count: Int = 1
) : ActionTarget() {
    override fun getTarget(session: MafiaSession, source: Option<MafiaPlayer>): List<MafiaPlayer> {
        return session.players.filter { player ->
            !(self && player == source.orNull()) &&
                    list.any { role -> role::class.isInstance(player.role) }
        }
    }
}

class AlignmentTarget(

)