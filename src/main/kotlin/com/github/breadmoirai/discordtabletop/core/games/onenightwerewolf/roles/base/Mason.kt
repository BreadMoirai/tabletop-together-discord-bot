package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base

import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.MutualLookAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.NightAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.ONWPlayer
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.ONWSession
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.OneNightWerewolfRole
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration

class Mason(number: Int = 0) : OneNightWerewolfRole(number) {
    override val wakeOrder: Int = 40
    override val isWerewolf: Boolean = false

    override suspend fun wakeUp(
        session: ONWSession, player: ONWPlayer, timeout: Duration
    ): List<NightAction> {
        logger.info(session, "${player.displayNameAndStartingCard(true)} is waking up")
        val otherMasons = session.playersToWake
            .filter { it.startingRole is Mason || (it.startingRole is Doppleganger && it.startingRole.copiedRole is Mason) }
        val masons = listOf(player, *otherMasons.toTypedArray())
        logger.info(session, buildString {
            append("Mason ")
            append(player.displayName())
            append(" has found the following masons: ")
            append(otherMasons.map { it.displayNameAndStartingCard(true) })
        })
        session.playersToWake.removeAll(otherMasons)
        val action = MutualLookAction(masons)
        val interactions = session.requestInteraction(masons, timeout, false)
        coroutineScope {
            interactions.forEach { (p, def) ->
                launch {
                    def.await().tap { it.hook.sendMessage("You woke up and did the following: " + action.textFor(p)).setEphemeral(true).queue() }
                }
            }
        }
        return listOf(action)
    }
}