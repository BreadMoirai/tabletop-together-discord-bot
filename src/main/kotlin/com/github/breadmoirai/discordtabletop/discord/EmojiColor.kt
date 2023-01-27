package com.github.breadmoirai.discordtabletop.discord

enum class EmojiColor(val code: String) {
    Black("⚫"),
    Blue("\uD83D\uDD35"),
    Brown("\uD83D\uDFE4"),
    Green("\uD83D\uDFE2"),
    Purple("\uD83D\uDFE3"),
    Orange("\uD83D\uDFE0"),
    Red("\uD83D\uDD34"),
    White("⚪"),
    Yellow("\uD83D\uDFE1");

    override fun toString(): String {
        return code
    }
}