package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base

import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.NightAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.ONWPlayer
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.ONWSession
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.RevealAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.OneNightWerewolfRole
import kotlin.time.Duration

class Minion : OneNightWerewolfRole() {
    override val wakeOrder: Int = 30
    override val isWerewolf: Boolean = false

    override suspend fun wakeUp(
        session: ONWSession,
        player: ONWPlayer,
        timeout: Duration
    ): List<NightAction> {
        val wolves = session.players.filter { it.startingRole.isWerewolf }
        val players = listOf(player, *wolves.toTypedArray())
        val interactions = session.requestInteraction(players.map(ONWPlayer::userId), timeout, false)
        val action = RevealAction(wolves, Werewolf(0), listOf(player), player.startingRole)
        interactions.forEach { (p, option) ->
            option.await().tap { interaction ->
                interaction.hook
                    .sendMessage(action.textFor(players.find { it.userId == p }!!))
                    .setEphemeral(true)
                    .queue()
            }
        }
        return listOf(action)
    }
}