package com.github.breadmoirai.discordtabletop.api.discord

enum class EmojiNumber(val code: String) {
    Zero("0️⃣"),
    One("1️⃣"),
    Two("2️⃣"),
    Three("3️⃣"),
    Four("4️⃣"),
    Five("5️⃣"),
    Six("6️⃣"),
    Seven("7️⃣"),
    Eight("8️⃣"),
    Nine("9️⃣"),
    Ten("🔟");

    companion object {
        fun ofRange(range: IntRange): List<EmojiNumber> {
            return EmojiNumber.values().slice(range)
        }
    }
}