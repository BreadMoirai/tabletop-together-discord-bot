package com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf

import com.github.breadmoirai.discordtabletop.core.InteractableSession.Companion.randomId
import com.github.breadmoirai.discordtabletop.core.games.GameLobby
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.roles.OneNightWerewolfRole
import com.github.breadmoirai.discordtabletop.discord.emoji
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.danger
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.interactions.components.success
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.InlineEmbed
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class ONWLobby(commandEvent: GenericCommandInteractionEvent) :
    GameLobby(OneNightWerewolf, 24.hours, commandEvent) {

    val roles: MutableList<OneNightWerewolfRole> = mutableListOf()
    val baseRoles = OneNightWerewolfRole.baseRoles()
    val nightTimer = 1.minutes
    private val editId = randomId("edit-game")

    override suspend fun launch() {
        bindButton(editId, ::onEditGame)
        super.launch()
    }

    private suspend fun onEditGame(event: ButtonInteractionEvent) {
        val editBaseRolesId = randomId("edit-base-roles")
        bindStringSelect(editBaseRolesId, ::onEditBaseRoles)
        logger.info(this, "Player ${event.member!!.effectiveName} is editing roles for ${game.name}")
        event.reply(MessageCreate {
            content = "Please select which roles to use"
            actionRow(StringSelectMenu(editBaseRolesId) {
                minValues = 0
                maxValues = baseRoles.size
                for (role in baseRoles) {
                    option(role.name, role.id, role.description, null, role in roles)
                }
            })
        }).setEphemeral(true).queue()
    }

    private suspend fun onEditBaseRoles(event: StringSelectInteractionEvent) {
        val rolesToSet = event.selectedOptions.map { option -> baseRoles.find { it.id == option.value }!! }
        roles.removeAll(baseRoles)
        roles.addAll(rolesToSet)
        event.deferEdit().queue()
        event.hook.deleteOriginal()
        // TODO: Add branch if hook is expired
        lastOriginalInteraction.hook.editOriginalEmbeds(Embed {
            buildEmbed().invoke(this)
        }).queue()
    }

    override fun addActionRows(): suspend InlineMessage<MessageCreateData>.() -> Unit = {
        actionRow(
            success(joinId, "Join"),
            danger(leaveId, "Leave"),
        )
        actionRow(
            emoji(startId, Emoji.fromUnicode("⚔️"), "Start Game"),
            emoji(editId, Emoji.fromUnicode("\uD83D\uDCCB"), "Edit Roles"),
            emoji(cancelId, Emoji.fromUnicode("\uD83D\uDDD1️"), "Cancel Game")
        )
    }

    override suspend fun buildEmbed(): suspend InlineEmbed.() -> Unit = {
        super.buildEmbed().invoke(this)
        field {
            name = "Roles"
            value = roles.joinToString("\n") { it.name }
        }
    }

}