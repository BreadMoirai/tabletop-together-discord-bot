package com.github.breadmoirai.discordtabletop.discord

import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.api.utils.Timestamp
import java.time.Instant

fun String.bold() = if (this.startsWith("\n")) "\n**${this.substring(1)}**" else "**$this**"
    fun String.underline() = "__${this}__"
fun String.italicize() = "_${this}_"
fun String.strikethrough() = "~${this}~"

fun Instant.toTimestamp(format: TimeFormat) = format.atInstant(this).toString()