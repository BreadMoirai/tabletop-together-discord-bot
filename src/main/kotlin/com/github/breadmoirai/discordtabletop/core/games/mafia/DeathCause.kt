package com.github.breadmoirai.discordtabletop.core.games.mafia

import com.github.breadmoirai.discordtabletop.util.oxfordAnd

sealed class DeathCause {
    open suspend fun getDeathMessage(player: MafiaPlayer): String {
        return "${player.displayName()} was killed!"
    }
}

class CondemnDeathCause(val condemners: List<MafiaPlayer>) : DeathCause() {
    override suspend fun getDeathMessage(player: MafiaPlayer): String {
        return "${player.displayName()} was condemned to death by ${condemners.oxfordAnd { it.displayName() }}"
    }
}