package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf

import com.github.breadmoirai.discordtabletop.core.games.TabletopGame
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.ComponentInteraction
import org.koin.core.component.KoinComponent
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object OneNightWerewolf : TabletopGame, KoinComponent {
    override val id = "onenightwerewolf"
    override val name = "One Night Werewolf"
    override val description = ""
    override val playerCount = 1..20

    override suspend fun openLobby(event: GenericCommandInteractionEvent) {
        val gameLobby = ONWLobby(event)
        gameLobby.launch()
        gameLobby.gameStarted.subscribe { _, startGameEvent ->
            println("Game started")
            // cancel()
            // should just get garbage collected as after the gameLobby closes
            ONWSession(
                startGameEvent,
                gameLobby.players,
                gameLobby.roles,
                gameLobby.nightTimer,
                5.minutes,
                30.seconds
            ).launch()
        }
    }
}

internal val errorResponses = listOf("Please stop... \uD83D\uDE2D", "I literally can't.", "My rate-limits! Please.")

internal fun ComponentInteraction.replyError(): Boolean {
    reply(errorResponses.random()).setEphemeral(true).queue()
    return false
}

// roles
val role_order = """
–9: Oracle
–8: Copycat
–8-A Mirror Man
–7: Doppelgänger
–6: Vampire/The Master/The Count
 –6-B: The Count
 –6-C: Renfield
–5: Diseased
–4: Cupid
–3: Instigator
–2: Priest
–1: Assassin
v1-B: Apprentice Assassin
00: Lovers
0: Sentinel 
1-A: Aliens/Synthetic/Groob/Zerb/Body Snatcher
 1-C: Cow
 1-D: Groob & Zerb
 1-F: Body Snatcher 
1-SV Villains (Temptress, Dr. Peeker, 
Rapscallion, and Henchman #7)
1-SV-E Evilometer
2: Werewolves
 2-B: Alpha Wolf
 2-C: Mystic Wolf
3: Minion
3-B: Apprentice Tanner
3-C: Leader
3-D Mad Scientist
3-E Intern
4: Masons
4-B: Thing (that goes Bump in the Night)
4-C Annoying Lad
5: Seer
5-B: Apprentice Seer
5-C: Paranormal Investigator
5-D: Marksman
5-E: Nostradamus
5-G: Psychic
5-H Detector
6: Robber
6-B: Witch
6-C: Pickpocket
6-D Role Retriever
6-E Voodoo Lou
7: Troublemaker
7-B: Village Idiot
7-C: Aura Seer
7-D: Gremlin
7-F: Rascal
7-G Switcheroo
8: Drunk
9: Insomniac
9-B Self-Awareness Girl
9-C: Squire
9-D Family Man
9-Z: Beholder
10: Revealed
10-B: Exposer
10-C Flipper
10-E: Empath
11: Curator
13: Blob
13-A: Mortician"""