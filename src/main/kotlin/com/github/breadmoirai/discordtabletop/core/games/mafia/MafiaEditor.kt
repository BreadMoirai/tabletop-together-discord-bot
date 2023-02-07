package com.github.breadmoirai.discordtabletop.core.games.mafia

import kweb.Kweb
import kweb.components.Component
import kweb.h1
import kweb.plugins.fomanticUI.fomanticUIPlugin
import kweb.route

object MafiaEditor {

    init {
        Kweb(port = 16097, plugins = listOf(fomanticUIPlugin)) {
            doc.body {
                route {
                    path("/setup/{playsetId}") { params ->
                        val lobby = Mafia.openLobbies.getOrElse("gameId") {
                            url.value = "/"
                            return@path
                        }

                    }
                }
            }
        }
    }

    fun Component.indexPage() {

    }
}