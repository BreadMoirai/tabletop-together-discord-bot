package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base.Doppleganger
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.base.Minion
import com.github.breadmoirai.discordtabletop.util.oxfordAnd

typealias ONWEndCondition = suspend (ONWSession, VoteList) -> Option<ONWConclusion>

object ONWEndConditions {
    suspend fun circleVote(session: ONWSession, voteList: VoteList): Option<ONWConclusion> {
        val players = session.players
        if (voteList.maxVotes != 1 || voteList.size != session.players.size) {
            return None
        }
        return onwConclusion(session, voteList) {
            messages += "No player received more than 1 vote..."
            if (players.all { it.team is VillagerTeam }) {
                winningTeams += players.map { it.team }.toSet()
                messages += "Villager Team wins! All players were on the villager team."
            } else if (players.any { it.team is WerewolfTeam }) {
                val werewolves = players.filter { it.team is WerewolfTeam }
                winningTeams += werewolves.map { it.team }
                if (werewolves.size == 1) {
                    messages +=
                        buildString {
                            append("Uh oh! There ")
                            if (werewolves.size == 1)
                                append("was a werewolf")
                            else
                                append("were werewolves")
                            append(" hidden among you! ")
                            append(werewolves.oxfordAnd {
                                it.displayMention()
                            })
                            append(" won!")
                        }
                }
            }
        }.some()
    }

    suspend fun tannerWin(session: ONWSession, voteList: VoteList): Option<ONWConclusion> {
        val deathList = voteList.deathList
        val tannerDeaths = deathList.filter { (p, _) -> p.team is TannerTeam }
        if (tannerDeaths.isEmpty()) {
            return None
        }
        return onwConclusion(session, voteList) {
            messages += buildString {
                append("You have sentenced ")
                append(deathList.oxfordAnd { it.first.displayMention() })
                append(" to death!")
            }
            winningTeams += tannerDeaths.map { (p, _) -> p.team }
            if (tannerDeaths != deathList)
                messages += "One of the players killed was a Tanner!"
            messages += deathList.oxfordAnd { (p, votes) -> "${p.displayMention()} wins as Tanner!" }
        }.some()
    }

    suspend fun villageOrWerewolfWin(session: ONWSession, voteList: VoteList): Option<ONWConclusion> {
        val deathList = voteList.deathList
        val werewolfDeaths = deathList.filter { (p, _) -> p.currentRole.isWerewolf }
        return onwConclusion(session, voteList) {
            messages += buildString {
                append("You have sentenced ")
                append(deathList.oxfordAnd { it.first.displayMention() })
                append(" to death!")
            }
            if (werewolfDeaths.isNotEmpty()) {
                winningTeams += players.map { it.team }.filterIsInstance<VillagerTeam>()
                val nonWerewolfDeaths = deathList.filter { (p, _) ->
                    !p.currentRole.isWerewolf
                }.filter {(p, _) ->
                    p.currentRole !is Minion
                }.filter {(p, _) ->
                    val currentRole = p.currentRole
                    !(currentRole is Doppleganger && currentRole.copiedRole is Minion)
                }
                for ((nonWerewolfDeath, _) in nonWerewolfDeaths) {
                    messages += buildString {
                        append(nonWerewolfDeath.displayMention())
                        append(" died as a ")
                        append(nonWerewolfDeath.currentRole.displayRole(false))
                    }
                }
                val minionDeaths = deathList.filter { (p, _) ->
                    val currentRole = p.currentRole
                    currentRole is Minion || (currentRole is Doppleganger && currentRole.copiedRole is Minion)
                }
                for ((minionDeath, _) in minionDeaths) {
                    messages += "${minionDeath.displayMention()} died as a ${minionDeath.currentRole.displayRole(false)}"
                }
                messages += werewolfDeaths.oxfordAnd { (p, _) ->
                    "${p.displayMention()} died as a ${p.currentRole.displayRole(false)}!"
                }
                messages += buildString {
                    append(players.filter { it.team is VillagerTeam }.oxfordAnd { it.displayMention() })
                    append(" won!")
                }
            } else {
                winningTeams += players.map { it.team }.filterIsInstance<WerewolfTeam>()
                val nonMinionDeaths = deathList.filter { (p, _) ->
                    val currentRole = p.currentRole
                    !(currentRole is Minion || currentRole is Doppleganger && currentRole.copiedRole is Minion)
                }
                for ((death, _) in nonMinionDeaths) {
                    messages += "${death.displayMention()} died as a ${death.currentRole.displayRole(false)}"
                }
                val minionDeaths = deathList.filter { (p, _) ->
                    val currentRole = p.currentRole
                    currentRole is Minion || (currentRole is Doppleganger && currentRole.copiedRole is Minion)
                }
                for ((minionDeath, _) in minionDeaths) {
                    messages += "${minionDeath.displayMention()} died as a ${minionDeath.currentRole.displayRole(false)}"
                }
            }
        }.some()
    }

}