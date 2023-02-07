package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf

internal typealias VoteList = List<Pair<ONWPlayer, List<ONWPlayer>>>

val VoteList.maxVotes: Int
    get() {
        return this.maxOf { (_, votes) -> votes.size }
    }

val VoteList.deathList: VoteList
    get() {
        val max = maxVotes
        return this.filter { (_, votes) -> votes.size == maxVotes }
    }