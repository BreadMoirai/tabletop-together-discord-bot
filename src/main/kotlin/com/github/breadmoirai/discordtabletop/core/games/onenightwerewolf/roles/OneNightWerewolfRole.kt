package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles

import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.NightAction
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.OneNightWerewolfPlayer
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.OneNightWerewolfSession
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base.Doppleganger
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base.Drunk
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base.Hunter
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base.Insomniac
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base.Mason
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base.Minion
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base.Robber
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base.Seer
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base.Tanner
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base.Troublemaker
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base.Villager
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base.Werewolf
import com.github.breadmoirai.discordtabletop.logging.logger
import kotlin.time.Duration

abstract class OneNightWerewolfRole(number: Int = 0) {
    protected val logger by logger()
    val description: String = ""
    val name: String = this::class.simpleName!!
    val id: String = "$name-$number"
    open val wakeOrder: Int? = null

    open suspend fun wakeUp(
        session: OneNightWerewolfSession,
        player: OneNightWerewolfPlayer,
        timeout: Duration
    ): List<NightAction> {
        throw NotImplementedError("#wakeUp is not implemented for class ${this::class.qualifiedName}")
    }

    fun displayRole(withNumber: Boolean): String {
//        return if (withNumber && number != 0) {
//            "$name ${number.emoji}"
//        } else {
        return "**$name**"
//        }
    }

    fun pluralized(): String {
        return if (name.endsWith("f")) "${name.dropLast(1)}ves" else "${name}s"
    }


    companion object {
        fun baseRoles(): List<OneNightWerewolfRole> {
            return listOf(
                Doppleganger(),
                Werewolf(1),
                Werewolf(2),
                Minion(),
                Mason(1),
                Mason(2),
                Seer(),
                Robber(),
                Troublemaker(),
                Drunk(),
                Insomniac(),
                Villager(1),
                Villager(2),
                Villager(3),
                Hunter(),
                Tanner()
            )
        }
    }

    abstract val isWerewolf: Boolean
}
