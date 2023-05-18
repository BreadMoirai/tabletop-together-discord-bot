@file:Suppress("FunctionName")

package com.github.breadmoirai.discordtabletop.core.games.frosthaven

import arrow.core.firstOrNone
import arrow.core.getOrElse
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.FHIcons.lost
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.building.FHBuildPhase
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.building.FHBuilding
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.building.FHBuildings
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.event.FHEventPhase
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.event.FHEventType
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.event.FHEvents
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.item.FHItem
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.item.FHItemSlot
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.item.FHItemViewer
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.item.FHItems
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.OneNightWerewolf.id
import com.github.breadmoirai.discordtabletop.discord.*
import com.github.breadmoirai.discordtabletop.jda.*
import com.github.breadmoirai.discordtabletop.storage.Storage
import com.github.breadmoirai.discordtabletop.util.oxfordAnd
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.await
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.events.onCommandAutocomplete
import dev.minn.jda.ktx.events.onComponent
import dev.minn.jda.ktx.interactions.commands.*
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.interactions.components.danger
import dev.minn.jda.ktx.interactions.components.primary
import dev.minn.jda.ktx.interactions.components.success
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.util.random
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import javax.swing.text.html.HTML.Tag.P
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object Frosthaven : KoinComponent {
    val jda: JDA by inject()
    val guild: Guild by inject(named("main"))

    fun onReady() {
        FHItemSlot
        FHEventType
        guild.updateCommands {
            slash("fh", "Frosthaven") {
                group("manage", "manage the campaign") {
                    restrict(true, DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    subcommand("create", "create a new campaign")
                    subcommand("save", "save the current campaign")
                    subcommand("add-player", "add a player to the campaign") {
                        option<Member>("player", "player to be added", required = true)
                    }
                    subcommand("remove-building", "remove a building from the campaign") {
                        option<String>("id", "id of building to remove", required = true)
                    }
                }
                subcommand("info", "show campaign info")
                subcommand("item", "show an item") {
                    option<String>("query", "item number or item name", required = true, autocomplete = true)
                }
                subcommand("event", "draw an event") {
                    option<String>("type", "type of event") {
                        choice("summer outpost", "summer outpost")
                        choice("summer road", "summer road")
                        choice("winter outpost", "winter outpost")
                        choice("winter road", "winter road")
                        choice("boat", "boat")
                    }
                }
                subcommand("build", "enter build phase")
                group("unlock", "unlock campaign content") {
                    subcommand("item", "add an item to the available items")
                }
                restrict(true)
            }
        }.queue()
        jda.onCommandAutocomplete("fh") { event ->
            when ("${event.fullCommandName} ${event.focusedOption.name}") {
                "fh item query" -> `fh item query`(event)
            }
        }
        jda.onCommand("fh") { event ->
            when (event.fullCommandName) {
                "fh" -> event.deferReply().queue()
                "fh manage create" -> `fh manage create`(event)
                "fh manage save" -> {
                    event.reply("Campaign saved!").setEphemeral(true).queue()
                    Storage.write(getCampaign(event) ?: return@onCommand)
                }
                "fh manage add-player" -> `fh manage add-player`(event)
                "fh manage remove-building" -> `fh manage remove-building`(event)
                "fh info" -> `fh info`(event)
                "fh item" -> `fh item`(event)
                "fh event" -> `fh event`(event)
                "fh build" -> FHBuildPhase(event, getCampaign(event) ?: return@onCommand)
            }
        }
    }

    private fun `fh manage create`(event: GenericCommandInteractionEvent) {
        val id = event.guildChannel.id
        Storage.write(
            Campaign(
                id,
                FHItems.startingDeck(id),
                FHEvents.startingDeck(id),
                FHBuildings.startingDeck(id),
                listOf()
            )
        )
        event.reply("New campaign created!").queue()
    }

    private fun `fh manage add-player`(event: GenericCommandInteractionEvent) {
        val campaign = getCampaign(event) ?: return
        val target = event.getOption("player", OptionMapping::getAsMember)!!
        campaign.players.add(FHPlayer(target.id))
        Storage.write(campaign)
        event.reply("${target.effectiveName} was added to the campaign!").queue()
    }

    private fun `fh manage remove-building`(event: GenericCommandInteractionEvent) {
        val campaign = getCampaign(event) ?: return
        val target = event.requireOption("id").asString
        campaign.buildings.buildings.removeIf { it.id == target }
    }

    private suspend fun `fh info`(event: GenericCommandInteractionEvent) {
        val campaign = getCampaign(event) ?: return
        event.deferReply().queue()
        event.interaction.hook.sendMessage(campaign.formatted).queue()
    }

    private suspend fun `fh item`(event: GenericCommandInteractionEvent) {
        val campaign = getCampaign(event) ?: return
        val queryRaw = event.requireOption("query").asString
        val query = queryRaw.lowercase().replace(Regex("\\s"), "")
        val match = campaign.items.items
            .firstOrNone { item -> query in item.index.lowercase().replace(" ", "") }
        match.tapNone {
            event.reply("No item found that matches $queryRaw").queue()
        }.tap { item ->
            FHItemViewer(event, item)
        }
    }

    private fun `fh item query`(event: CommandAutoCompleteInteractionEvent) {
        val campaign = getCampaign(event) ?: return
        val query = event.focusedOption.value.lowercase().replace(Regex("\\s"), "")
        if (query.isBlank()) {
            event.replyChoiceStrings(campaign.items.items.take(25).map(FHItem::index)).queue()
            return
        }
        val matches =
            campaign.items.items
                .filter { item -> query in item.index.lowercase().replace(" ", "") }
                .take(25)
                .map(FHItem::index)
        event.replyChoiceStrings(matches).queue()
    }

    private suspend fun `fh event`(event: GenericCommandInteractionEvent) {
        val campaign = getCampaign(event) ?: return
        val type = event.getOption("type")?.asString
        if (type == null) {
            event.reply("No event type specified...").setEphemeral(true).queue()
            return
        }
        val randomEvent = campaign.events.drawEvent(FHEventType.valueOf(type)).getOrElse {
            event.reply("No events left of type ${FHEventType.valueOf(type).text}...").queue()
            return
        }
        FHEventPhase(event, campaign, randomEvent)
    }

    private val campaigns: MutableMap<String, Campaign> = mutableMapOf()
    private fun getCampaign(event: GenericInteractionCreateEvent): Campaign? {
        return campaigns.getOrPut(event.guildChannel.id) {
            Storage.read<Campaign>("id" to event.guildChannel.id).getOrElse {
                if (event is GenericCommandInteractionEvent)
                    event.reply("No Campaign found...").queue()
                return null
            }
        }
    }
}