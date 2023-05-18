package com.github.breadmoirai.discordtabletop.discord

import com.github.breadmoirai.discordtabletop.storage.StorableId
import com.github.breadmoirai.discordtabletop.storage.StorableTransient
import com.github.breadmoirai.discordtabletop.storage.Storage
import jetbrains.exodus.bindings.BindingUtils
import net.dv8tion.jda.api.entities.Guild
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

sealed class EmojiColor(val circle: String, val square: String) : Comparable<EmojiColor> {
    @StorableId
    val name = this::class.simpleName!!
    @StorableTransient
    val ordinal: Int
        get() = values().indexOf(this)

    object Black : EmojiColor("⚫", "⬛")
    object Blue : EmojiColor("\uD83D\uDD35", "\uD83D\uDFE6")
    object Brown : EmojiColor("\uD83D\uDFE4", "\uD83D\uDFEB")
    object Green : EmojiColor("\uD83D\uDFE2", "\uD83D\uDFE9")
    object Purple : EmojiColor("\uD83D\uDFE3", "\uD83D\uDFEA")
    object Orange : EmojiColor("\uD83D\uDFE0", "\uD83D\uDFE7")
    object Red : EmojiColor("\uD83D\uDD34", "\uD83D\uDFE5")
    object White : EmojiColor("⚪", "⬜")
    object Yellow : EmojiColor("\uD83D\uDFE1", "\uD83D\uDFE8")
    object Clear : EmojiColor(
        emojiSource.retrieveEmojiById("1061180216652873758").complete().formatted,
        emojiSource.retrieveEmojiById("1061180216652873758").complete().formatted
    )

    override fun compareTo(other: EmojiColor): Int {
        return ordinal.compareTo(other.ordinal)
    }

    companion object : KoinComponent {
        private val emojiSource by inject<Guild>(named("main"))

        init {
            Storage.registerPropertyType(read = { read ->
                valueOf(BindingUtils.readString(read))
            }, write = { write, value ->
                write.writeString(value.name)
            })
        }

        fun values(): Array<EmojiColor> {
            return arrayOf(Black, Blue, Brown, Green, Purple, Orange, Red, White, Yellow, Clear)
        }

        fun valueOf(value: String): EmojiColor {
            return when (value) {
                "Black" -> Black
                "Blue" -> Blue
                "Brown" -> Brown
                "Green" -> Green
                "Purple" -> Purple
                "Orange" -> Orange
                "Red" -> Red
                "White" -> White
                "Yellow" -> Yellow
                "Clear" -> Clear
                else -> throw IllegalArgumentException("No object com.github.breadmoirai.discordtabletop.discord.EmojiColor.$value")
            }
        }
    }

}