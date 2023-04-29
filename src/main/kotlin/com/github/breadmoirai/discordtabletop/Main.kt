package com.github.breadmoirai.discordtabletop

import com.github.breadmoirai.discordtabletop.core.games.mafia.MafiaPlayer
import com.github.breadmoirai.discordtabletop.core.games.onenightwerewolf.OneNightWerewolf
import com.github.breadmoirai.discordtabletop.jda.requireOption
import com.github.breadmoirai.discordtabletop.logging.logger
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.choice
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.updateCommands
import dev.minn.jda.ktx.jdabuilder.light
import dev.minn.jda.ktx.messages.MessageCreate
import kweb.InputType
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.TimeFormat
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

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
    val mainGuild = jda.getGuildById(1032201616272666694) ?: error("Bot must be in main guild to function")
    mainGuild.updateCommands {
        slash("play", "Play a game") {
            restrict(guild = true)
            option<String>("game", "What game to play", true) {
//                    choice(ConnectFour.name, ConnectFour.id)
                choice(OneNightWerewolf.name, OneNightWerewolf.id)
            }
        }
    }
//    if (botNew) {
//        jda.updateCommands {
//            slash("play", "Play a game") {
//                restrict(guild = true)
//                option<String>("game", "What game to play", true) {
////                    choice(ConnectFour.name, ConnectFour.id)
//                    choice(OneNightWerewolf.name, OneNightWerewolf.id)
//                }
//            }
//        }.queue()
//    }
    val discord = module {
        single { jda }
        single(named("main")) {
            jda.getGuildById(1032201616272666694) ?: error("Bot must be in main guild to function")
        }
    }
    startKoin {
        modules(discord)
    }

    jda.onCommand("play") { event ->
        val game = event.requireOption("game").asString
        logger.info("\\play game=$game")
        when (game) {
//            ConnectFour.id -> {
//                ConnectFour.openLobby(event)
//            }
            OneNightWerewolf.id -> {
                OneNightWerewolf.openLobby(event)
            }
        }
    }


}
