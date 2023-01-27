package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base

import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.OneNightWerewolfRole

class Villager(number: Int = 0) : OneNightWerewolfRole(number) {
    override val isWerewolf: Boolean = false
}