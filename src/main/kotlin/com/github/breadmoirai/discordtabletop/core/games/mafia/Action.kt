package com.github.breadmoirai.discordtabletop.core.games.mafia

sealed class Action {
    val id = this::class.simpleName
}

class TargetedAction : Action() {
    lateinit var target: ActionTarget
}
