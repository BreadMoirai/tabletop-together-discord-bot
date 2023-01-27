package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.alien

import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.OneNightWerewolfRole

class Alien : OneNightWerewolfRole() {
    override val wakeOrder: Int = 11
    override val isWerewolf: Boolean = false
    val notes = """
        Just Stare at each other
        View a card from any odd player
        Each View a card from any even player
        
    """.trimIndent()
}