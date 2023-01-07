package com.github.breadmoirai.discordtabletop.core.api

import com.github.breadmoirai.discordtabletop.api.core.api.reactive.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StateTest {

    @Test
    fun `set value`() {
        runBlocking {
            val state = State("first")
            println("state.value = ${state.value}")
            assertEquals("first", state.value)
            state.set("second")
            println("state.value = ${state.value}")
            assertEquals("second", state.value)
            state.set("third")
            println("state.value = ${state.value}")
            assertEquals("third", state.value)
        }
    }

    @Test
    fun `subscribe new values`() {
        runBlocking {
            val state = State("first")
            val testValues = mutableListOf("first", "second", "third")
            val subscriber = state.subscribe(this) { value ->
                println("value = $value")
                assert(testValues.isNotEmpty())
                assertEquals(value, testValues.removeFirst())

                if (testValues.isEmpty()) cancel()
            }
            delay(10)
            state.set("second")
            state.set("third")
        }
    }

    @Test
    fun `multiple subscribers`() {
        runBlocking {
            val state = State("first")
           repeat(5) {
               val testValues = mutableListOf("first", "second", "third")
               state.subscribe(this) { value ->
                   println("[$it] value = $value")
                   assert(testValues.isNotEmpty())
                   assertEquals(value, testValues.removeFirst())

                   if (testValues.isEmpty()) cancel()
               }
           }
            delay(10)
            state.set("second")
            state.set("third")
        }
    }

    @Test
    fun `multiple emitters and subscribers`() {
        runBlocking {
            val state = State(0)
            val emitterValues = IntRange(1, 10000).toMutableList().shuffled()
            repeat(10) {
                val testValues = IntRange(0, 10000).toMutableSet()
                state.subscribe(this) { value ->
//                    println("[$it] value = $value")
                    if (value == -1) {
                        assert(testValues.isEmpty())
                        cancel()
                        return@subscribe
                    }
                    assert(testValues.isNotEmpty())
                    assert(testValues.remove(value))


                }
            }
            delay(100)
            val chunked = emitterValues.chunked(500)
            val jobs = mutableListOf<Job>()
            for (chunk in chunked) {
                jobs.add(launch {
                  for (i in chunk) {
                      state.set(i)
                  }
                })
            }
            joinAll(*jobs.toTypedArray())
            state.set(-1)
        }
    }
}