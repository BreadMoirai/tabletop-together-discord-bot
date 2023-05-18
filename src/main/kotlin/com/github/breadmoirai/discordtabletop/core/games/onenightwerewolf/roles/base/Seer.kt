package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base

import arrow.core.getOrElse
import com.github.breadmoirai.discordtabletop.core.interactable.InteractableSession.Companion.randomId
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.NightAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.NoAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.ONWPlayer
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.ONWSession
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.PeekAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.OneNightWerewolfRole
import com.github.breadmoirai.discordtabletop.discord.emoji
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.interactions.components.buttons.Button
import kotlin.time.Duration


class Seer : OneNightWerewolfRole() {
    override val wakeOrder: Int = 50
    override val isWerewolf: Boolean = false

    companion object {
        private const val VIEW_ONE_PLAYER = "View one other player's card"
        private const val VIEW_TWO_CENTER = "View two cards from the center"
        private const val WAKE_MESSAGE = "Please chose to view 2 roles from the center or 1 role from another player"
        private const val VIEW_ONE_PLAYER_MESSAGE = "Please chose which player's cards to view"
        private const val VIEW_TWO_CENTER_MESSAGE = "Please chose which center cards to view"
    }

    override suspend fun wakeUp(
        session: ONWSession,
        player: ONWPlayer,
        timeout: Duration
    ): List<NightAction> {
        val viewOnePlayerId = randomId("view-one")
        val viewTwoCenterId = randomId("view-two")
        val selected = session.selectButton(
            player,
            WAKE_MESSAGE,
            listOf(
                Button.primary(viewOnePlayerId, VIEW_ONE_PLAYER).withEmoji(1.emoji),
                Button.primary(viewTwoCenterId, VIEW_TWO_CENTER).withEmoji(2.emoji)
            ),
            timeout,
            false,
        ).getOrElse {
            return listOf(NoAction(player))
        }
        if (selected.componentId == viewOnePlayerId) {
            val (reply, t) = session.selectOtherPlayer(player, VIEW_ONE_PLAYER_MESSAGE, timeout).getOrElse {
                return listOf(NoAction(player))
            }
            val target = t.single()
            val action = PeekAction(player, target)
            reply.editMessage(MessageEdit(components = listOf(), content = action.text)).queue()
            return listOf(action)
        } else {
            val (reply, t) = session.selectOtherPlayer(
                player,
                VIEW_TWO_CENTER_MESSAGE,
                timeout,
                session.center,
                count = 2
            ).getOrElse { return listOf(NoAction(player)) }
            val action = PeekAction(player, t)
            reply.editMessage(MessageEdit(components = listOf(), content = action.text)).queue()
            return listOf(action)
        }
    }
}