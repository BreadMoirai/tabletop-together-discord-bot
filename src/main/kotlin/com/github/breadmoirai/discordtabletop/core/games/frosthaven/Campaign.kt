package com.github.breadmoirai.discordtabletop.core.games.frosthaven

import com.github.breadmoirai.discordtabletop.core.games.frosthaven.Frosthaven.guild
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.building.FHBuilding
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.building.FHBuildingDeck
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.event.FHEvent
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.event.FHEventDeck
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.item.FHItemDeck
import com.github.breadmoirai.discordtabletop.discord.EmojiColor
import com.github.breadmoirai.discordtabletop.discord.bold
import com.github.breadmoirai.discordtabletop.storage.Storable
import com.github.breadmoirai.discordtabletop.storage.StorableId
import com.github.breadmoirai.discordtabletop.util.boxfordAnd
import com.github.breadmoirai.discordtabletop.util.oxfordAnd
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.generics.getChannel
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class Campaign(
    @StorableId val id: String,
    val items: FHItemDeck,
    val events: FHEventDeck,
    val buildings: FHBuildingDeck,
    players: List<FHPlayer>
) : Storable, KoinComponent {
    val players: MutableList<FHPlayer> = players.toMutableList()
    val jda: JDA by inject()
    val formatted: String
        get() = buildString {
            val guild = jda.getChannel<GuildChannel>(id)!!.guild
            append("Frosthaven Campaign".bold())
            if (players.isNotEmpty()) {
                append("\nPlayers: ".bold())
                append(players.boxfordAnd { player ->
                    guild.retrieveMemberById(player.id).complete()?.effectiveName
                        ?: "<unknown user:${player.id}>"
                })
            }
            append(buildings.formatted)
            append("\n")
            append(events.formatted)
            append("\n")
            append(items.formatted)
        }
}