package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base

import arrow.core.getOrElse
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.NightAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.NoAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.OneNightWerewolfPlayer
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.OneNightWerewolfSession
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.SwapAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.OneNightWerewolfRole
import kotlin.time.Duration

class Troublemaker : OneNightWerewolfRole() {
    override val wakeOrder: Int = 70
    override val isWerewolf: Boolean = false

    override suspend fun wakeUp(
        session: OneNightWerewolfSession,
        player: OneNightWerewolfPlayer,
        timeout: Duration
    ): List<NightAction> {
        val (event, selected) = session.selectOtherPlayer(
            player,
            "Select two other players to swap",
            timeout,
            count = 2
        ).getOrElse {
            return listOf(NoAction(player))
        }
        val action = SwapAction(player, selected[0], selected[1])
        event.editMessage(action.text).queue()
        return listOf(action)
    }
}