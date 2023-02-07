package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base

import arrow.core.getOrElse
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.NightAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.NoAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.ONWPlayer
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.ONWSession
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.PeekAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.OneNightWerewolfRole
import kotlin.time.Duration

class Insomniac : OneNightWerewolfRole() {
    override val wakeOrder: Int = 90
    override val isWerewolf: Boolean = false

    override suspend fun wakeUp(
        session: ONWSession,
        player: ONWPlayer,
        timeout: Duration
    ): List<NightAction> {
        val interaction = session.awaitInteraction(player, timeout, false).getOrElse {
            return listOf(NoAction(player))
        }
        val action = PeekAction(player, player)
        interaction.hook.sendMessage(action.text).setEphemeral(true).queue()
        return listOf(action)
    }
}