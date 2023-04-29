package com.github.breadmoirai.discordtabletop.core.games.mafia

class MafiaRole(val name: String, val alignment: Alignment, val tags: List<MafiaTag>) {
    fun copy(): MafiaRole {
        return MafiaRole(name, alignment, tags)
    }
}


