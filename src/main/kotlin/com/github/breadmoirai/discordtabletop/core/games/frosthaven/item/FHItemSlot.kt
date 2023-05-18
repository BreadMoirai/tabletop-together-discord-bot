package com.github.breadmoirai.discordtabletop.core.games.frosthaven.item

import com.github.breadmoirai.discordtabletop.storage.Storage
import jetbrains.exodus.bindings.BindingUtils

enum class FHItemSlot {
    Head, Body, Legs, OneHand, TwoHand, Bag, Plot;

    companion object {
        init {
            Storage.registerPropertyType(read = { read ->
                FHItemSlot.valueOf(BindingUtils.readString(read))
            }, write = { write, value ->
                write.writeString(value.name)
            })
        }
    }
}