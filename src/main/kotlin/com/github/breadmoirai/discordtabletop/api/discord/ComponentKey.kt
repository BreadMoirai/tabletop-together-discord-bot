package com.github.breadmoirai.discordtabletop.api.discord

class ComponentKey(val componentId: String, val messageId: Long) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ComponentKey
        return if (messageId != that.messageId) false else componentId == that.componentId
    }

    override fun hashCode(): Int {
        var result = (messageId xor (messageId ushr 32)).toInt()
        result = 31 * result + componentId.hashCode()
        return result
    }
}