package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base

import arrow.core.getOrElse
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.NightAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.NoAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.ONWPlayer
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.ONWSession
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.PeekAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.SwapAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.OneNightWerewolfRole
import dev.minn.jda.ktx.messages.MessageEdit
import kotlin.time.Duration

class Robber : OneNightWerewolfRole() {
    override val wakeOrder: Int = 60
    override val isWerewolf: Boolean = false

    override suspend fun wakeUp(
        session: ONWSession,
        player: ONWPlayer,
        timeout: Duration
    ): List<NightAction> {
        logger.info(session, "${player.displayNameAndStartingCard(true)} is waking up")
        val req = session.selectOtherPlayer(player, "Select another player to rob", timeout).getOrElse {
            return listOf(NoAction(player))
        }
        val (interaction, otherPlayer) = req
        logger.info(
            session,
            buildString {
                append(player.displayNameAndStartingCard(true))
                append(" selected ")
                append(otherPlayer.single().displayNameAndStartingCard(true))
                append(" to rob")
            }
        )
        val swap = SwapAction(player, otherPlayer.single())
        val peek = PeekAction(player, player)
        interaction.editMessage(MessageEdit("${swap.text}\n${peek.text}", components = listOf())).queue()
        return listOf(swap, peek)
    }
}