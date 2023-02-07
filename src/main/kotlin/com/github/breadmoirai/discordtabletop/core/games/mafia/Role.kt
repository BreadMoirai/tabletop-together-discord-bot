package com.github.breadmoirai.discordtabletop.core.games.mafia

class Role(val name: String, val alignment: Alignment) {
    fun copy(): Role {
        return Role(name, alignment)
    }
}

