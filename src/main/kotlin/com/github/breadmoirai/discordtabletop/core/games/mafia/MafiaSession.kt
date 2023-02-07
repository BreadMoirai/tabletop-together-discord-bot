package com.github.breadmoirai.discordtabletop.core.games.mafia

import com.github.breadmoirai.discordtabletop.core.games.GameSession
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import kotlin.time.Duration

class MafiaSession(
    inactivityLimit: Duration,
    trackedUsers: List<Long>,
    initialInteraction: IReplyCallback,
) : GameSession<MafiaPlayer>(inactivityLimit, trackedUsers, initialInteraction) {

    override val players: List<MafiaPlayer> = mutableListOf()


}
