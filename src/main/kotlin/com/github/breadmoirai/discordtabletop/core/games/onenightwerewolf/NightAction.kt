package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf

import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.OneNightWerewolfRole
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base.Doppleganger
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base.Tanner
import com.github.breadmoirai.discordtabletop.util.oxfordAnd
import kotlinx.coroutines.runBlocking

sealed class NightAction {
    abstract val log: String
    abstract val text: String

    open suspend fun textFor(player: OneNightWerewolfPlayer): String {
        return text
    }
}

class NoAction(val player: OneNightWerewolfPlayer) : NightAction() {
    override val log: String =
        runBlocking { "${player.displayNameAndStartingCard(true)} woke up and failed to do anything" }
    override val text: String = runBlocking { "You woke up and failed to do anything" }
}

class StartAction(val player: OneNightWerewolfPlayer) : NightAction() {
    override val log: String = runBlocking { "${player.displayName()} is a ${player.startingRole.displayRole(true)}" }
    override val text: String = runBlocking { "You are a ${player.startingRole.displayRole(false)}" }
}

class CopyRoleAction(
    val source: OneNightWerewolfPlayer,
    val target: OneNightWerewolfPlayer
) : NightAction() {
    override val log: String =
        runBlocking {
            "${source.displayNameAndStartingCard(true)} " +
                    "copied ${target.displayNameAndCard(true)}'s card: " +
                    target.currentRole.displayRole(true)
        }
    override val text: String =
        runBlocking { "You copied ${target.displayName()}'s card: ${target.currentRole.displayRole(false)}" }

    init {
        val role = source.startingRole
        assert(role is Doppleganger) { "Only Doppleganger role may copy other roles" }
        (role as Doppleganger).copiedRole = target.currentRole
        if (target.currentRole is Tanner) {
            source.swapTeams(TannerTeam())
        } else {
            source.swapTeams(target.team)
        }
    }
}

class MutualLookAction(val participants: List<OneNightWerewolfPlayer>) : NightAction() {
    override val log: String = runBlocking {
        if (participants.size == 1) "${participants.first().displayName()} looked at no one"
        else "${participants.oxfordAnd { it.displayName() }} looked at each other"
    }
    override val text: String = runBlocking {
        if (participants.size == 1) "You looked at no one"
        else "You made eye contact with ${participants.oxfordAnd { it.displayName() }}"
    }

    override suspend fun textFor(player: OneNightWerewolfPlayer): String {
        assert(player in participants) { "Can only generate text for players involved" }
        val others = participants.filter { it != player }
        return if (participants.size == 1) "You looked at no one"
        else "You made eye contact with ${others.oxfordAnd { it.displayName() }}"
    }
}

class RevealAction(
    val source: List<OneNightWerewolfPlayer>,
    val sourceRole: OneNightWerewolfRole,
    val audience: List<OneNightWerewolfPlayer>,
    val audienceRole: OneNightWerewolfRole
) : NightAction() {
    constructor(
        source: OneNightWerewolfPlayer,
        sourceRole: OneNightWerewolfRole,
        audience: OneNightWerewolfPlayer,
        audienceRole: OneNightWerewolfRole
    ) : this(listOf(source), sourceRole, listOf(audience), audienceRole)

    init {
        assert(source.all { it !in audience }) { "The revealed may not reveal to themselves" }
    }

    override val log: String = runBlocking {
        "${source.oxfordAnd { it.displayNameAndStartingCard(true) }} " +
                "revealed themselves as ${sourceRole.displayRole(false)} " +
                "to ${audience.oxfordAnd { it.displayNameAndStartingCard(true) }}"
    }
    override val text: String = runBlocking {
        "You have revealed your role as a ${sourceRole.displayRole(false)} to any ${audienceRole.displayRole(false)}(s)"
    }

    override suspend fun textFor(player: OneNightWerewolfPlayer): String {
        assert(player in source || player in audience) { "Player must be involved to generate text" }
        return if (player in source) buildString {
            append("You have revealed your role as a ")
            append(sourceRole.displayRole(false))
            append(" to any ")
            append(audienceRole.displayRole(false))
        }
        else buildString {
            append(source.oxfordAnd { it.displayName() })
            append(" have revealed themselves as ")
            if (source.size == 1) {
                append("a ")
                append(sourceRole.displayRole(false))
            } else {
                append(sourceRole.pluralized())
            }
        }

    }
}

class PeekAction(
    val source: OneNightWerewolfPlayer,
    val target: List<OneNightWerewolfPlayer>
) : NightAction() {
    constructor(source: OneNightWerewolfPlayer, target: OneNightWerewolfPlayer) : this(source, listOf(target))

    override val log: String = runBlocking {
        when {
            target.size == 1 && target.single() == source -> buildString {
                append(source.displayName())
                append(" peeked at their own card and saw ")
                append(target.single().currentRole.displayRole(true))
            }
            target.size == 1 && target.single() is CenterCard -> buildString {
                append(source.displayName())
                append(" peeked at the ")
                append(target.single().displayName())
                append(" card in the center and saw ")
                append(target.single().currentRole.displayRole(true))
            }
            target.size == 1 -> buildString {
                append(source.displayName())
                append(" peeked at ")
                append(target.single().displayName())
                append("'s card and saw ")
                append(target.single().currentRole.displayRole(true))
            }
            target.all { it is CenterCard } -> buildString {
                append(source.displayName())
                append(" peeked at the following cards in the center and saw ")
                append(target.oxfordAnd {
                    buildString {
                        append(it.displayName())
                        append(" as ")
                        append(it.currentRole.displayRole(true))
                    }
                })
                append(target.oxfordAnd { it.displayNameAndCard(true) })
            }
            else -> buildString {
                append(source.displayName())
                append(" peeked at ")
                append(target.oxfordAnd {
                    buildString {
                        append(it.displayName())
                        append("'s card and saw ")
                        append(it.currentRole.displayRole(true))
                    }
                })
            }
        }
    }

    override val text: String = runBlocking {
        when {
            target.size == 1 && target.single() == source -> buildString {
                append("You peeked at your own card and saw ")
                append(target.single().currentRole.displayRole(false))
            }
            target.size == 1 && target.single() is CenterCard -> buildString {
                append("You peeked at the ")
                append(target.single().displayName())
                append(" card in the center and saw ")
                append(target.single().currentRole.displayRole(false))
            }
            target.size == 1 -> buildString {
                append("You peeked at ")
                append(target.single().displayName())
                append("'s card and saw ")
                append(target.single().currentRole.displayRole(false))
            }
            target.all { it is CenterCard } -> buildString {
                append("You peeked at the following cards in the center and saw ")
                append(target.oxfordAnd {
                    buildString {
                        append(it.displayName())
                        append(" as ")
                        append(it.currentRole.displayRole(false))
                    }
                })
                append(target.oxfordAnd { it.displayNameAndCard(false) })
            }
            else -> buildString {
                append("You peeked at ")
                append(target.oxfordAnd {
                    buildString {
                        append(it.displayName())
                        append("'s card and saw ")
                        append(it.currentRole.displayRole(false))
                    }
                })
            }
        }
    }
}

class SwapAction(
    val source: OneNightWerewolfPlayer,
    val first: OneNightWerewolfPlayer,
    val second: OneNightWerewolfPlayer
) : NightAction() {
    constructor(source: OneNightWerewolfPlayer, target: OneNightWerewolfPlayer) : this(source, source, target)

    override val log: String = runBlocking {
        if (source == first) {
            if (second is CenterCard)
                "${source.displayName()} swapped cards with the ${second.displayName()} in the center"
            else
                "${source.displayName()} swapped cards with ${second.displayName()}"
        } else {
            if (second is CenterCard)
                "${source.displayName()} swapped ${first.displayName()}'s card with the ${second.displayName()} card in the center"
            else
                "${source.displayName()} swapped ${first.displayName()}'s card and ${second.displayName()}'s card"
        }
    }

    override val text: String = runBlocking {
        if (source == first) {
            if (second is CenterCard)
                "You swapped cards with the ${second.displayName()} in the center"
            else
                "You swapped cards with ${second.displayName()}"
        } else {
            if (second is CenterCard)
                "You swapped ${first.displayName()}'s card with the ${second.displayName()} card in the center"
            else
                "You swapped ${first.displayName()}'s card and ${second.displayName()}'s card"
        }
    }

    init {
        assert(second != source) { "If Swap is self swap, source must be first" }
        assert(first !is CenterCard) { "If swap involves a CenterCard, the CenterCard must be second" }
        val frole = first.currentRole
        val fteam = first.team
        first.currentRole = second.currentRole
        first.swapTeams(second.team)
        second.currentRole = frole
        second.swapTeams(fteam)
    }
}