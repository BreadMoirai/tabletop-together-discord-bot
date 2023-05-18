package com.github.breadmoirai.discordtabletop.core.games.frosthaven

import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji

object FHIcons {
    val returnToDeck: RichCustomEmoji
    val nonReturnToDeck: RichCustomEmoji
    val flip: RichCustomEmoji
    val lost: RichCustomEmoji
    val build: RichCustomEmoji
    val upgrade: RichCustomEmoji
    val prosperity: RichCustomEmoji
    val arrowvine: RichCustomEmoji
    val axenut: RichCustomEmoji
    val corpsecap: RichCustomEmoji
    val flamefruit: RichCustomEmoji
    val rockroot: RichCustomEmoji
    val snowthistle: RichCustomEmoji
    val hide: RichCustomEmoji
    val metal: RichCustomEmoji
    val lumber: RichCustomEmoji
    val coin: RichCustomEmoji
    val wrecked: RichCustomEmoji
    val morale1 : RichCustomEmoji
    val morale2 : RichCustomEmoji
    val morale3 : RichCustomEmoji
    val blank : RichCustomEmoji

    init {
        val emojiList = Frosthaven.guild.retrieveEmojis().complete()
        returnToDeck = emojiList.find { it.name == "fh_return" }!!
        nonReturnToDeck = emojiList.find { it.name == "fh_non_return" }!!
        flip = emojiList.find { it.name == "fh_flip" }!!
        lost = emojiList.find { it.name == "fh_lost" }!!
        build = emojiList.find { it.name == "fh_build" }!!
        upgrade = emojiList.find { it.name == "fh_upgrade" }!!
        prosperity = emojiList.find { it.name == "fh_prosperity" }!!
        arrowvine = emojiList.find { it.name == "fh_arrowvine" }!!
        axenut = emojiList.find { it.name == "fh_axenut" }!!
        corpsecap = emojiList.find { it.name == "fh_corpsecap" }!!
        flamefruit = emojiList.find { it.name == "fh_flamefruit" }!!
        rockroot = emojiList.find { it.name == "fh_rockroot" }!!
        snowthistle = emojiList.find { it.name == "fh_snowthistle" }!!
        hide = emojiList.find { it.name == "fh_hide" }!!
        metal = emojiList.find { it.name == "fh_metal" }!!
        lumber = emojiList.find { it.name == "fh_lumber" }!!
        coin = emojiList.find { it.name == "fh_coin" }!!
        wrecked = emojiList.find { it.name == "fh_wrecked" }!!
        morale1 = emojiList.find { it.name == "fh_morale_cost_1" }!!
        morale2 = emojiList.find { it.name == "fh_morale_cost_2" }!!
        morale3 = emojiList.find { it.name == "fh_morale_cost_3" }!!
        blank = emojiList.find { it.name == "blank_space" }!!
    }

    fun urlFor(number: Number): String {
        assert(number in 1..12)
        val folder = "https://github.com/any2cards/worldhaven/raw/master/images/tokens/frosthaven/scenario-aids"
        return "$folder/fh-scenario-aid-number-${"$number".padStart(2, '0')}.png"
    }
}