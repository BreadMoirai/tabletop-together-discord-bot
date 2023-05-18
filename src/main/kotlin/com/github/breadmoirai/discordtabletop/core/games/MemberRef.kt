package com.github.breadmoirai.discordtabletop.core.games

import net.dv8tion.jda.api.entities.Member

interface MemberRef {
    fun member(): Member
}
