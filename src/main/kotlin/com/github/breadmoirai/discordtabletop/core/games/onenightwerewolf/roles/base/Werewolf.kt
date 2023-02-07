package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base

import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.MutualLookAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.NightAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.ONWPlayer
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.ONWSession
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.OneNightWerewolfRole
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration

class Werewolf(number: Int = 0) : OneNightWerewolfRole(number) {
    override val wakeOrder: Int = 20
    override val isWerewolf: Boolean = true

    override suspend fun wakeUp(
        session: ONWSession,
        player: ONWPlayer,
        timeout: Duration
    ): List<NightAction> {
        logger.info(session, "${player.displayNameAndStartingCard(true)} is waking up")
        val otherWerewolves = session.playersToWake
            .filter { it.startingRole is Werewolf || (it.startingRole is Doppleganger && it.startingRole.copiedRole is Werewolf) }
        val wolves = listOf(player, *otherWerewolves.toTypedArray())
        logger.info(session, buildString {
            append("Werewolf ")
            append(player.displayName())
            append(" has found the following werewolves: ")
            append(otherWerewolves.map { it.displayNameAndStartingCard(true) })
        })
        val action = MutualLookAction(wolves)
        val interactions = session.requestInteraction(wolves, timeout, false)
        coroutineScope {
            interactions.forEach { (p, def) ->
                launch {
                    def.await().tap {
                        it.hook.sendMessage("You woke up and did the following: " + action.textFor(p))
                            .setEphemeral(true).queue()
                    }
                }
            }
        }
        return listOf(action)
    }
}

