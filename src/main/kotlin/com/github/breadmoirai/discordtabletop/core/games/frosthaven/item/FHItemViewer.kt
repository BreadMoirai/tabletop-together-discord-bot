package com.github.breadmoirai.discordtabletop.core.games.frosthaven.item

import com.github.breadmoirai.discordtabletop.core.BaseInteractableSession
import com.github.breadmoirai.discordtabletop.core.InteractableSession.Companion.randomId
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.FHIcons
import com.github.breadmoirai.discordtabletop.discord.editOriginalComponents
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.success
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class FHItemViewer(interaction: IReplyCallback, val item: FHItem) : BaseInteractableSession(
    5.minutes,
    listOf(),
    interaction
) {

    init {
        val flipFrontButtonId = randomId("fh-item-flip-front")
        val flipBackButtonId = randomId("fh-item-flip-back")
        interaction.reply(MessageCreate(embeds = listOf(item.embedFront()))).queue {
            it.editOriginalComponents(success(flipFrontButtonId, "Show Back", FHIcons.flip))
                .queueAfter(5, TimeUnit.SECONDS)
            it.retrieveOriginal().queue { message ->
                messageId = message.idLong
            }
        }
        bindButton(flipFrontButtonId) { event ->
            event.editMessage(MessageEdit(embeds = listOf(item.embedBack()), components = listOf())).await()
            delay(5.seconds)
            event.hook.editOriginalComponents(success(flipBackButtonId, "Show Front", FHIcons.flip)).queue()
        }
        bindButton(flipFrontButtonId) { event ->
            event.editMessage(MessageEdit(embeds = listOf(item.embedBack()), components = listOf())).await()
            delay(5.seconds)
            event.hook.editOriginalComponents(success(flipBackButtonId, "Show Front", FHIcons.flip)).queue()
        }
        onCancel.subscribe { id ->
            interaction.messageChannel.editMessageComponentsById(id, listOf()).queue()
        }

    }


}