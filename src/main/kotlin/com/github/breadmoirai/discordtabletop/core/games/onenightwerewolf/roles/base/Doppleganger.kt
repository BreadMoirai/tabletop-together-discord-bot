package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base

import arrow.core.getOrElse
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.CopyRoleAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.NightAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.NoAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.ONWPlayer
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.ONWSession
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.OneNightWerewolfRole
import dev.minn.jda.ktx.messages.MessageEdit
import kotlin.reflect.full.createInstance
import kotlin.time.Duration

class Doppleganger : OneNightWerewolfRole() {
    override var wakeOrder: Int = -9
    lateinit var copiedRole: OneNightWerewolfRole
    override val isWerewolf: Boolean
        get() {
            return copiedRole.isWerewolf
        }

    override suspend fun wakeUp(
        session: ONWSession,
        player: ONWPlayer,
        timeout: Duration
    ): List<NightAction> {
        if (this::copiedRole.isInitialized) {
            return copiedRole.wakeUp(session, player, timeout)
        }
        val (response, t) = session.selectOtherPlayer(
            player,
            "Please select a player whose role you would like to copy!",
            timeout = timeout
        ).getOrElse { return listOf(NoAction(player)) }
        val target = t.single()
        val copyRoleAction = CopyRoleAction(player, target)
        response.editMessage(MessageEdit(copyRoleAction.text, components = listOf())).queue()
        val actions = mutableListOf<NightAction>(copyRoleAction)
        copiedRole = target.currentRole::class.createInstance()
        when (copiedRole) {
            is Villager, is Hunter, is Tanner -> {
                // pass
            }
            is Seer, is Robber, is Troublemaker, is Drunk -> {
                actions.addAll(copiedRole.wakeUp(session, player, timeout))
            }
            is Werewolf, is Minion, is Mason, is Insomniac -> {
                wakeOrder = copiedRole.wakeOrder!!
                session.playersToWake.add(0, player)
                session.playersToWake.sortBy { it.startingRole.wakeOrder }
            }
            else -> {
                error("No behavior defined for doppleganger to copy ${copiedRole.name}")
            }
        }
        return actions
    }


}