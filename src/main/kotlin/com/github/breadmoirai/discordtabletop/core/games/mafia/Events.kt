package com.github.breadmoirai.discordtabletop.core.games.mafia

import com.github.breadmoirai.discordtabletop.reactive.Cancellable

interface MafiaEvent {
    val session: MafiaSession
}

class ActionEvent(
    override val session: MafiaSession,
    actions: List<Action>,
) : MafiaEvent {
    val actions = actions.toMutableList()
}

