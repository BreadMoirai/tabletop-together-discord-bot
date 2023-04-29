package com.github.breadmoirai.discordtabletop.core.games.mafia

import dev.minn.jda.ktx.messages.send

abstract class Action {
    val id
        get() = this::class.simpleName

    abstract suspend fun apply(session: MafiaSession, event: ActionEvent)
}

abstract class TargetedAction(val source: MafiaPlayer, val target: MafiaPlayer) : Action() {

}

open class VisitAction(source: MafiaPlayer, target: MafiaPlayer) : TargetedAction(source, target) {
    override suspend fun apply(session: MafiaSession, event: ActionEvent) {
        TODO("Not yet implemented")
    }
}

class CondemnAction(source: MafiaPlayer, target: MafiaPlayer) : TargetedAction(source, target) {
    override suspend fun apply(session: MafiaSession, event: ActionEvent) {
        session.channel.sendMessage(buildString {
            append(source.displayName())
            append(" condemned ")
            append(target.displayName())
        }).queue()
    }
}

class CancelCondemnAction(val source: MafiaPlayer) : Action() {
    override suspend fun apply(session: MafiaSession, event: ActionEvent) {
        session.channel.sendMessage("${source.displayName()} rescinded their condemn")
    }
}



class DeathAction(val target: MafiaPlayer, val cause: DeathCause) : Action() {
    override suspend fun apply(session: MafiaSession, event: ActionEvent) {
        if
        session.channel.sendMessage("${target.displayName()} was killed!").queue()
        session.channel.sendMessage("${target.displayName()} was a ${target.revealedRole}!").queue()
    }
}

class AttackAction(source: MafiaPlayer, target: MafiaPlayer, val message: String) : VisitAction(source, target) {
    override suspend fun apply(session: MafiaSession, event: ActionEvent) {
        if (event.actions.filterIsInstance<ProtectAction>().any { protect -> protect.target == this.target }) {
            session.sendPrivateMessage(event.)
        }
    }
}

class ProtectAction(source: MafiaPlayer, target: MafiaPlayer, val level: Int) : VisitAction(source, target) {
    override suspend fun apply(session: MafiaSession, event: ActionEvent) {
    }
}



