package com.github.breadmoirai.discordtabletop

import com.github.breadmoirai.discordtabletop.api.core.connect4.ConnectFour
import com.github.breadmoirai.discordtabletop.api.discord.InteractionManager
import com.github.breadmoirai.discordtabletop.api.discord.InteractionManagerImpl
import com.github.breadmoirai.discordtabletop.api.jda.requireOption
import com.github.breadmoirai.discordtabletop.api.logging.logger
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.choice
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.updateCommands
import dev.minn.jda.ktx.jdabuilder.light
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.requests.GatewayIntent
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun main() {
    val logger by logger()
    val config: Config = ConfigFactory.load()
    val jda: JDA = light(
        config.getString("bot.token"),
        intents = listOf(
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_VOICE_STATES
        ),
        enableCoroutines = true
    ).awaitReady()

    val botNew = config.getBoolean("bot.new")
    logger.info("config['bot.new'] = $botNew")
    if (botNew) {
        jda.updateCommands {
            slash("play", "Play a game") {
                restrict(guild = true)
                option<String>("game", "What game to play", true) {
                    choice(ConnectFour.name, ConnectFour.id)
                }
            }
        }.queue()
    }
    val discord = module {
        single { jda }
        single<InteractionManager> { InteractionManagerImpl(jda) }
        single<CustomEmoji>(named("blank"), true) { jda.getGuildById(1032201616272666694)!!.retrieveEmojiById(1061180216652873758).complete()!! }
    }
    startKoin {
        modules(discord)
    }

    jda.onCommand("play") { event ->
        val game = event.requireOption("game").asString
        logger.info("\\play game=$game")
        when (game) {
            ConnectFour.id -> {
                ConnectFour.createLobby(event)
            }
        }
    }


}
