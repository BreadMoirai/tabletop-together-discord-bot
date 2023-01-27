package com.github.breadmoirai.discordtabletop.discord

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.emoji.Emoji
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

private object EmojiNumbers : KoinComponent {
    private val numbers = mutableMapOf<Int, Emoji>(
        0 to Emoji.fromUnicode("0️⃣"),
        1 to Emoji.fromUnicode("1️⃣"),
        2 to Emoji.fromUnicode("2️⃣"),
        3 to Emoji.fromUnicode("3️⃣"),
        4 to Emoji.fromUnicode("4️⃣"),
        5 to Emoji.fromUnicode("5️⃣"),
        6 to Emoji.fromUnicode("6️⃣"),
        7 to Emoji.fromUnicode("7️⃣"),
        8 to Emoji.fromUnicode("8️⃣"),
        9 to Emoji.fromUnicode("9️⃣"),
        10 to Emoji.fromUnicode("🔟")
    )

    private val emojiSource by inject<Guild>(named("main"))

    init {
        val emojis = emojiSource.retrieveEmojis().complete()
        for (emoji in emojis) {
            val x = emoji.name.toUIntOrNull() ?: continue
            numbers[x.toInt()] = emoji
        }
    }

    fun getEmojiForNumber(num: Int): Emoji {
        assert(num in 0..42) { "Emojis are only available for numbers 0 to 42" }
        return numbers[num]!!
    }

}


val Int.emoji: Emoji
    get() {
        return EmojiNumbers.getEmojiForNumber(this)
    }