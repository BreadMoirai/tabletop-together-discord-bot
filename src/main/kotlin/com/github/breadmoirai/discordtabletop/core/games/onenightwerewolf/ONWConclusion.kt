package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf

import com.github.breadmoirai.discordtabletop.core.BaseInteractableSession
import com.github.breadmoirai.discordtabletop.core.InteractableSession.Companion.randomId
import com.github.breadmoirai.discordtabletop.util.buildString
import dev.minn.jda.ktx.interactions.components.danger
import dev.minn.jda.ktx.interactions.components.primary
import dev.minn.jda.ktx.interactions.components.success
import dev.minn.jda.ktx.messages.MessageCreate
import kotlin.time.Duration.Companion.hours

class ONWConclusion(
    val session: ONWSession, val messages: List<String>, val winningTeams: Set<ONWTeam>, val votes: VoteList
) : BaseInteractableSession(24.hours, session.trackedUsers, session.lastInteraction) {
    val players = session.players

    override suspend fun launch() {
        channel.sendMessage(MessageCreate {
            embed {
                title = "One Night Werewolf Game Summary"
                description = buildString {
                    for (winningTeam in winningTeams) {
                        append("**")
                        append(winningTeam.name)
                        append(" Win!**")
                        for (player in winningTeam.players) {
                            append("\n   ")
                            append(player.displayMentionAndCards(false))
                            val deadPlayers = votes.deathList.map { (p, _) -> p }
                            if (player in deadPlayers)
                                append(" ☠️")
                        }
                        append("\n")
                    }
                }
                for (losingTeam in session.teams.filter { it !in winningTeams }) {
                    field {
                        title = "${losingTeam.name} Loses! :("
                        value = buildString {
                            losingTeam.players.joinToString("\n") {
                                it.displayMentionAndCards(false)
                            }
                        }.replace(Regex.fromLiteral("\\[(.*)\\]")) { match -> "**[${match.groupValues[1]}]**" }
                    }
                }
                val viewActions = randomId("view-actions")
                val viewCards = randomId("view-cards")
                val viewVotes = randomId("view-votes")
                actionRow(
                    primary(viewActions, "View Night Actions"),
                    success(viewCards, "View Player Cards"),
                    danger(viewVotes, "View Death Votes")
                )
                bindButton(viewActions) { event ->
                    event.reply(MessageCreate {
                        embed {
                            title = "Night Actions"
                            description = session.actions
                                .filter { it !is StartAction }
                                .joinToString("\n", transform = NightAction::log)
                        }
                    }).setEphemeral(true).queue()
                }
                bindButton(viewCards) { event ->
                    event.reply(MessageCreate {
                        embed {
                            title = "Starting Cards"
                            description = buildString {
                                append(players.joinToString("\n") { it.displayNameAndCards(true) })
                                append("\n\n")
                                for (center in session.center) {
                                    append(center.displayName())
                                    append(" Center (")
                                    append(center.startingRole.displayRole(true))
                                    append(")")
                                    if (center.startingRole != center.currentRole) {
                                        append(" -> [")
                                        append(center.currentRole.displayRole(true))
                                        append("]")
                                    }
                                }
                            }
                        }
                    }).setEphemeral(true).queue()
                }
                bindButton(viewVotes) { event ->
                    event.reply(MessageCreate {
                        embed {
                            title = "Death Votes"
                            for ((d, p) in votes) {
                                field {
                                    title = "${d.displayNameAndCards(true)} received **${p.size}** votes"
                                    description = p.joinToString("\n") {
                                        it.displayNameAndCards(true)
                                    }
                                }
                            }
                        }
                    }).setEphemeral(true).queue()
                }
            }
        }).queue()
    }
}

class ONWConclusionBuilder(
    val session: ONWSession, val votes: VoteList
) {
    val messages = mutableListOf<String>()
    val winningTeams = mutableSetOf<ONWTeam>()
    val players = session.players
    val nightActions = session.actions
}

suspend fun onwConclusion(
    session: ONWSession, votes: VoteList, block: suspend ONWConclusionBuilder.() -> Unit
): ONWConclusion {
    return ONWConclusionBuilder(session, votes).apply { block(this) }.run {
        ONWConclusion(session, messages, winningTeams, votes)
    }
}