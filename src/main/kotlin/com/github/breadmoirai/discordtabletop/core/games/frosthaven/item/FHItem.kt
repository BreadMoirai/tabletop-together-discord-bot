package com.github.breadmoirai.discordtabletop.core.games.frosthaven.item

import com.github.breadmoirai.discordtabletop.storage.Storable
import com.github.breadmoirai.discordtabletop.storage.StorableId
import com.github.breadmoirai.discordtabletop.storage.StorableTransient
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.MessageEmbed

data class FHItem(@StorableId val num: String, val name: String, val slot: FHItemSlot, val front: String) : Storable {
    val back = front.replace(".png", "-back.png")

    @StorableTransient
    val index = "$num $name"

    fun embedFront(): MessageEmbed = Embed {
        title = name
        footer {
            name = "fh-${num.padStart(3, '0')}-${this@FHItem.name.lowercase().replace(' ', '-')}"
        }
        image = front
    }

    fun embedBack(): MessageEmbed = Embed {
        title = name
        footer {
            name = "fh-${num.padStart(3, '0')}-${this@FHItem.name.lowercase().replace(' ', '-')}-back"
        }
        image = back
    }




}