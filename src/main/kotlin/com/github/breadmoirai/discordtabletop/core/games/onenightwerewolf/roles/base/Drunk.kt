package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base

import arrow.core.getOrElse
import com.github.breadmoirai.discordtabletop.core.InteractableSession.Companion.randomId
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.NightAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.NoAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.ONWPlayer
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.ONWSession
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.SwapAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.OneNightWerewolfRole
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.interactions.components.buttons.Button
import kotlin.time.Duration

class Drunk : OneNightWerewolfRole() {
    override val wakeOrder: Int = 80
    override val isWerewolf: Boolean = false

    override suspend fun wakeUp(
        session: ONWSession,
        player: ONWPlayer,
        timeout: Duration
    ): List<NightAction> {
        val centerButtonIds = session.center.map { randomId("center-card${it.userId}") to it }
        val selectedButton = session.selectButton(
            player,
            "Please select a Center Card to swap with",
            centerButtonIds.map { (id, card) -> Button.primary(id, card.emoji) },
            timeout,
            false
        ).getOrElse { return listOf(NoAction(player)) }
        val target = centerButtonIds.find { (id, p) -> id == selectedButton.componentId }!!.second
        val swap = SwapAction(player, target)
        selectedButton.hook.editOriginal(MessageEdit(swap.text, components = emptyList())).queue()
        return listOf(swap)
    }
}