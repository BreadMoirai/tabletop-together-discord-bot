package com.github.breadmoirai.discordtabletop.core.games.frosthaven.event

import com.github.breadmoirai.discordtabletop.storage.Storable
import com.github.breadmoirai.discordtabletop.storage.StorableId
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.MessageEmbed

data class FHEvent(val type: FHEventType, val num: Int) : Storable {
    companion object {
        private val endpoint = "https://github.com/any2cards/worldhaven/raw/master/images/events/frosthaven"
    }

    @StorableId
    val id: String = "${type.text} $num"
    val front: String = "$endpoint/${type.folder}/fh-${type.abbr}-${"%02d".format(num)}-f.png"
    val back: String = "$endpoint/${type.folder}/fh-${type.abbr}-${"%02d".format(num)}-b.png"

    fun embedFront(): MessageEmbed = Embed {
        title = "${type.text} Event"
        footer {
            name = "${type.abbr}-${"%02d".format(num)}-f"
        }
        image = front
    }

    fun embedBack(): MessageEmbed = Embed {
        title = "${type.text} Event"
        footer {
            name = "${type.abbr}-${"%02d".format(num)}-b"
        }
        image = back
    }
}

