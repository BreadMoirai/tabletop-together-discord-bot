package com.github.breadmoirai.discordtabletop.core.api

import com.github.breadmoirai.discordtabletop.storage.Storable
import com.github.breadmoirai.discordtabletop.storage.StorableId
import com.github.breadmoirai.discordtabletop.storage.Storage
import dev.minn.jda.ktx.interactions.components.link
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class StorableTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup(): Unit {

        }
    }


    data class Plain(@StorableId val id: String, val num: Int) : Storable

    data class Linked(
        @StorableId val id: String,
        val num: Int,
        val truth: Boolean,
        val msg: String,
        val link: Plain?
    ) : Storable

    data class MultiValued(
        @StorableId val id: String,
        val nums: List<Int>,
        val truths: List<Boolean>
    ) : Storable

    data class MultiLinked(
        @StorableId val id: String,
        val links: List<Plain>
    ) : Storable

    @Test
    fun `store plain obj`() {
        val write = Plain("unique", 0)
        println("write = $write")
        Storage.write(write)

        val read = Storage.read<Plain>("id" to "unique")
        println("read = ${read.orNull()}")
        assertEquals(write, read.orNull()!!, message = "Test Read")
    }

    @Test
    fun `store linked obj`() {
        val singleLink = Plain("singleLink", 1)
        println("singleLink = $singleLink")
        val linkedObj = Linked("linkedObj", 1, false, "My Msg", singleLink)
        println("linkedObj = $linkedObj")
        Storage.write(linkedObj)

        val readLinkedObj = Storage.read<Linked>("id" to "linkedObj")
        println("readLinkedObj = ${readLinkedObj.orNull()}")
        val readSingleLink = Storage.read<Plain>("id" to "singleLink")
        println("readSingleLink = ${readSingleLink.orNull()}")


        assertEquals(linkedObj, readLinkedObj.orNull()!!, message = "Test Read")
        assertEquals(singleLink, readSingleLink.orNull()!!, message = "Test Read Linked")
    }

    @Test
    fun `update linked obj`() {
        val singleLink = Plain("singleLink", 1)
        println("singleLink = $singleLink")
        val linkedObj = Linked("linkedObj", 1, true, "My Msg", singleLink)
        println("linkedObj = $linkedObj")
        Storage.write(linkedObj)

        var readLinkedObj = Storage.read<Linked>("id" to "linkedObj")
        println("readLinkedObj = ${readLinkedObj.orNull()}")
        val readSingleLink = Storage.read<Plain>("id" to "singleLink")
        println("readSingleLink = ${readSingleLink.orNull()}")

        assertEquals(linkedObj, readLinkedObj.orNull()!!, message = "Test Read")
        assertEquals(singleLink, readSingleLink.orNull()!!, message = "Test Read Linked")

        val updatedLinkedObj = linkedObj.copy(truth = false)
        Storage.write(updatedLinkedObj)
        readLinkedObj = Storage.read("id" to "linkedObj")
        println("readLinkedObj = ${readLinkedObj.orNull()}")
        assertNotEquals(linkedObj, readLinkedObj.orNull()!!, message = "Test Read")
        assertEquals(updatedLinkedObj, readLinkedObj.orNull()!!, message = "Test Read")
    }

    @Test
    fun `multi valued obj`() {
        val multi = MultiValued("multi_valued", (1..20).toList(), listOf(true, false).sorted())
        Storage.write(multi)

        val multiRead = Storage.read<MultiValued>("id" to "multi_valued")
        assertEquals(multi, multiRead.orNull(), "MultiValued")

        val multiUpdate = MultiValued("multi_valued", (1..10).toList(), listOf(false).sorted())
        Storage.write(multiUpdate)

        val multiReadUpdate = Storage.read<MultiValued>("id" to "multi_valued")
        assertEquals(multiUpdate, multiReadUpdate.orNull(), "MultiValued")
    }

   @Test
    fun `multi linked obj`() {
        val link0 = Plain("link_0", 0)
        val link1 = Plain("link_1", 1)
        val link2 = Plain("link_2", 2)
        val link3 = Plain("link_3", 3)

        val multi = MultiLinked("multi_linked", listOf(link0, link1, link2, link3))
        Storage.write(multi)

        val multiRead = Storage.read<MultiLinked>("id" to "multi_linked")
        assertEquals(multi, multiRead.orNull(), "multi_linked")
       val link0Read = Storage.read<Plain>("id" to "link_0")
        assertEquals(link0, link0Read.orNull(), "link_0")
       val link1Read = Storage.read<Plain>("id" to "link_1")
        assertEquals(link1, link1Read.orNull(), "link_1")
       val link2Read = Storage.read<Plain>("id" to "link_2")
        assertEquals(link2, link2Read.orNull(), "link_2")
       val link3Read = Storage.read<Plain>("id" to "link_3")
        assertEquals(link3, link3Read.orNull(), "link_3")
    }

    @Test
    fun `nullable linked obj`() {
        val plain = Plain("plain", 0)
        val linked = Linked("linked",   0, false, "ok", plain)

        Storage.write(linked)
        val linkedRead = Storage.read<Linked>("id" to "linked")
        assertEquals(linked, linkedRead.orNull(), "linked")

        val linkedNull = linked.copy(link = null)
        Storage.write(linkedNull)
        val linkedNullRead = Storage.read<Linked>("id" to "linked")
        assertEquals(linkedNull, linkedNullRead.orNull(), "linkedNull")


    }
}