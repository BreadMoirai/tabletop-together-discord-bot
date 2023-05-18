package com.github.breadmoirai.discordtabletop.core.api

import com.github.breadmoirai.discordtabletop.core.games.frosthaven.FHItems
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class FHItemsTest {

    @Test
    fun `test get items`() {
        val all = FHItems.getItems(1..264)
        for (item in FHItems.ITEMS) {
            assertContains(all, item)
        }
        assertEquals(FHItems.ITEMS.size, all.size, "all items size")
        val coins = FHItems.getItems(245..245)
        coins.forEach { assert(it.num.startsWith("245")) }
        assertEquals(4, coins.size, "coins size")
    }
}