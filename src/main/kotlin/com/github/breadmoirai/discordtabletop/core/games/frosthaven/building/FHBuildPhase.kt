package com.github.breadmoirai.discordtabletop.core.games.frosthaven.building

import com.github.breadmoirai.discordtabletop.core.BaseInteractableSession
import com.github.breadmoirai.discordtabletop.core.InteractableSession.Companion.randomId
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.Campaign
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.FHIcons
import com.github.breadmoirai.discordtabletop.discord.editOriginalComponents
import com.github.breadmoirai.discordtabletop.storage.Storage
import dev.minn.jda.ktx.interactions.components.*
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.utils.messages.MessageEditData
import kotlin.time.Duration.Companion.minutes

class FHBuildPhase(interaction: IReplyCallback, val campaign: Campaign) : BaseInteractableSession(
    15.minutes,
    campaign.players.map { it.id.toLong() },
    interaction
) {

    init {
        val selectionId = randomId("fh-build-select")
        val confirmId = randomId("fh-build-confirm")
        interaction.messageChannel.sendMessage(MessageCreate {
            content = campaign.buildings.formatted
        }).queue()
        interaction.reply(MessageCreate {
            embed { title = "Select a building to upgrade..." }
        }).queue {
            it.retrieveOriginal().queue { message ->
                messageId = message.idLong
                it.editOriginalComponents(buildingSelectMenu(selectionId)).queue()
            }
        }
        bindStringSelect(selectionId) { buildSelectEvent ->
            val upgradeId = randomId("fh-build-upgrade")
            val cancelId = randomId("fh-build-cancel")
            val previewId = randomId("fh-build-preview")
            val unpreviewId = randomId("fh-build-unpreview")
            val chosenId = buildSelectEvent.selectedOptions.single().value
            val building = campaign.buildings.buildings.find { it.id == chosenId }!!
            buildSelectEvent.editMessage(show(building, upgradeId, cancelId, previewId, unpreviewId, false)).queue()
            bindButton(previewId) { event ->
                event.editMessage(show(building, upgradeId, cancelId, previewId, unpreviewId, true)).queue()
            }
            bindButton(unpreviewId) { event ->
                event.editMessage(show(building, upgradeId, cancelId, previewId, unpreviewId, false)).queue()
            }
            bindButton(cancelId) { event ->
                event.editMessage(MessageEdit {
                    embed { title = "Select a building to upgrade..." }
                    actionRow(buildingSelectMenu(selectionId))
                }).queue()
            }
            bindButton(upgradeId) { event ->
                val upgrade = campaign.buildings.upgrade(building)
                event.editMessage(MessageEdit(components = listOf()) {
                    embed {
                        title = if (building.level == 0)
                            "${upgrade.name} upgraded to level ${upgrade.level}"
                        else
                            "${upgrade.name} built!"
                        author {
                            iconUrl = FHIcons.urlFor(upgrade.level)
                        }
                        thumbnail = if (building.level == 0)
                            FHIcons.build.imageUrl
                        else
                            FHIcons.upgrade.imageUrl
                        image = upgrade.front
                        footer {
                            name = buildString {
                                append("fh-")
                                append(upgrade.id.padStart(2, '0'))
                                append("-")
                                append(upgrade.name.lowercase().replace(' ', '-'))
                                if (upgrade.id.toIntOrNull() != null) {
                                    append("-level-")
                                    append(upgrade.level)
                                }
                            }
                        }
                    }
                    embed {
                        image = upgrade.sticker
                    }
                }).queue()
                this.cleanup()
            }
            onCancel.subscribe { id ->
                channel.deleteMessageById(id).queue()
            }
        }
    }

    private fun show(
        building: FHBuilding,
        upgradeId: String,
        cancelId: String,
        previewId: String,
        unpreviewId: String,
        showPreview: Boolean
    ) = MessageEdit {
        val previewBuilding = FHBuildings.getBuilding(building.id, building.level + 1)
        embed {
            title = building.name
            author {
                iconUrl = if (building.level == 0) FHIcons.build.imageUrl
                else FHIcons.urlFor(building.level)
                if (showPreview)
                    name = "UPGRADE PREVIEW"
            }
            image = if (showPreview) previewBuilding.front
            else if (building.level == 0) building.sticker
            else building.front
            if (showPreview)
                description = buildString {
                    append("Upgrade Cost ")
                    append(building.level)
                    append(" -> ")
                    append(previewBuilding.level)
                    append("\n\t")
                    for (cost in building.upgradeCost) {
                        append(cost.formatted)
                        append("  ")
                    }
                }
            footer {
                name = buildString {
                    append("fh-")
                    append(building.id.padStart(2, '0'))
                    append("-")
                    append(building.name.lowercase().replace(' ', '-'))
                    if (building.id.toIntOrNull() != null) {
                        append("-level-")
                        if (showPreview)
                            append(previewBuilding.level)
                        else
                            append(building.level)
                    }
                }
            }
        }
        if (building.id.toIntOrNull() == null) {
            actionRow(
                success(upgradeId, "Build", FHIcons.build),
                danger(cancelId, "Cancel", FHIcons.lost)
            )
        } else if (showPreview) {
            actionRow(
                primary(unpreviewId, "Un-Preview", Emoji.fromUnicode("⏬️")),
                success(upgradeId, "Build", FHIcons.build),
                danger(cancelId, "Cancel", FHIcons.lost)
            )
        } else {
            actionRow(
                primary(previewId, "Preview", Emoji.fromUnicode("⏫")),
                success(upgradeId, "Build", FHIcons.build),
                danger(cancelId, "Cancel", FHIcons.lost)
            )
        }
    }

    private fun InlineMessage<MessageEditData>.embedFor(
        building: FHBuilding
    ) {
    }

    private fun buildingSelectMenu(buildingSelectionId: String) = StringSelectMenu(buildingSelectionId) {
        for (building in campaign.buildings.buildings.sortedBy { it.id }) {
            if (building.upgradeCost.isNotEmpty()) {
                if (building.level == 0)
                    option(building.id, building.id, building.name, FHIcons.build)
                else {
                    val desc = "${building.name} ${building.level} -> ${building.level + 1}"
                    option(building.id, building.id, desc, FHIcons.upgrade)
                }
            }
        }
    }

}
