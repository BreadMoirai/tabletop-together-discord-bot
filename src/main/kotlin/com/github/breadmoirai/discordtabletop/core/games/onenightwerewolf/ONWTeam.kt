package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf

import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.OneNightWerewolfRole
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
import kotlin.reflect.KClass

abstract class ONWTeam {
    abstract val name: String
    val players: MutableList<ONWPlayer> = mutableListOf()
    abstract val defaultRoles: Set<KClass<out OneNightWerewolfRole>>

    fun roleInTeamByDefault(role: OneNightWerewolfRole): Boolean {
        return defaultRoles.any { it.isInstance(role) }
    }
}

class WerewolfTeam : ONWTeam() {
    override val name = "Werewolves"
    override val defaultRoles = setOf(Werewolf::class, Minion::class)
}

class VillagerTeam : ONWTeam() {
    override val name = "Villagers"
    override val defaultRoles =
        setOf(
            Doppleganger::class,
            Mason::class,
            Seer::class,
            Robber::class,
            Troublemaker::class,
            Drunk::class,
            Insomniac::class,
            Villager::class,
            Hunter::class
        )
}

class TannerTeam : ONWTeam() {
    override val name = "Tanner"
    override val defaultRoles = setOf(Tanner::class)
}
