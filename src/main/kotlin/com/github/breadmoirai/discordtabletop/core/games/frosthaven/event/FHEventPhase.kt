package com.github.breadmoirai.discordtabletop.core.games.frosthaven.event

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import com.github.breadmoirai.discordtabletop.core.BaseInteractableSession
import com.github.breadmoirai.discordtabletop.core.InteractableSession.Companion.randomId
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.Campaign
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.FHIcons
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.FHPlayer
import com.github.breadmoirai.discordtabletop.core.games.frosthaven.Frosthaven
import com.github.breadmoirai.discordtabletop.discord.await
import com.github.breadmoirai.discordtabletop.discord.editComponents
import com.github.breadmoirai.discordtabletop.discord.editMessageComponents
import com.github.breadmoirai.discordtabletop.discord.editOriginalComponents
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.await
import dev.minn.jda.ktx.interactions.components.danger
import dev.minn.jda.ktx.interactions.components.primary
import dev.minn.jda.ktx.interactions.components.success
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class FHEventPhase(interaction: IReplyCallback, val campaign: Campaign, val eventCard: FHEvent) :
    BaseInteractableSession(
        5.minutes,
        campaign.players.map { it.id.toLong() },
        interaction
    ) {

    init {
        val flipButtonId = randomId("fh-event-flip")
        val confirmFlipButtonId = randomId("fh-event-flip")
        val returnButtonId = randomId("fh-event-return")
        val lostButtonId = randomId("fh-event-lost")
        var flipJob: Option<Job> = None
        val hook = interaction.reply(MessageCreate { embed { image = eventCard.front } }).queue {
            it.editOriginalComponents(
                success(flipButtonId, "Flip event card", FHIcons.flip)
            ).queueAfter(5, TimeUnit.SECONDS)
        }
        bindButton(flipButtonId) { event ->
            if (event.user.id !in campaign.players.map(FHPlayer::id)) {
                event.reply("Nice try, you aren't a part of this.").setEphemeral(true).queue()
                return@bindButton
            }
            event.editComponents(
                danger(
                    confirmFlipButtonId, "Cancel - Flipping card in 5 seconds", FHIcons.lost
                )
            ).await()
            flipJob = GlobalScope.launch {
                delay(6.seconds)
                flipJob = None
                event.hook.editOriginalComponents(listOf()).await()
                delay(2.seconds)
                val back = event.hook.sendMessage(MessageCreate { embed { image = eventCard.back } }).await()
                back.editMessageComponents(
                    success(returnButtonId, "Return to event deck", FHIcons.returnToDeck),
                    primary(lostButtonId, "Remove from game", FHIcons.nonReturnToDeck)
                ).queueAfter(5, TimeUnit.SECONDS)
            }.some()
        }
        bindButton(confirmFlipButtonId) { event ->
            flipJob.tap { it.cancel() }
            event.editComponents(
                success(flipButtonId, "Flip event card", FHIcons.flip)
            ).queue()
        }
        bindButton(returnButtonId) { event ->
            campaign.events.returnEvent(eventCard)
            event.editComponents(listOf()).queue()
            event.hook.sendMessage(buildString {
                append(eventCard.type.abbr.uppercase())
                append("-")
                append(eventCard.num)
                append(" was returned to the bottom of the ")
                append(eventCard.type.text)
                append(" event deck")
            }).queue()
        }
        bindButton(lostButtonId) { event ->
            event.editComponents(listOf()).queue()
            event.hook.sendMessage(buildString {
                append(eventCard.type.abbr.uppercase())
                append("-")
                append(eventCard.num)
                append(" was lost forever... ")
            }).queue()
        }
    }


}