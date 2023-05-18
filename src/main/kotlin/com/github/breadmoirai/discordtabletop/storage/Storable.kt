package com.github.breadmoirai.discordtabletop.storage

import jetbrains.exodus.entitystore.Entity
import kotlin.reflect.KClass
import kotlin.reflect.full.*

interface Storable {
    @StorableTransient val storableType: String
        get() = storableType(this::class)

    @StorableTransient val storableId: Pair<String, Comparable<*>>
        get() {
            @Suppress("UNCHECKED_CAST")
            for (property in (this::class as KClass<Any>).memberProperties) {
//                println("Found property ${property.name}, @StorableId = ${property.annotations}")
                if (property.hasAnnotation<StorableId>()) {
                    val value = property.get(this)
                    assert(value is Comparable<*>) { "StorableId field `${this::class.simpleName}#${property.name}` must be of type Comparable" }
                    return property.name to value as Comparable<*>
                }
            }
            throw ExceptionInInitializerError("Storable type ${this::class.simpleName} has no property marked with @StorableId")
        }

    companion object {
         fun storableType(cls: KClass<out Storable>): String {
            val clsQueue = mutableListOf<KClass<*>>(cls)
            while (clsQueue.isNotEmpty()) {
                val pop = clsQueue.removeFirst()
                if (pop.superclasses.contains(Storable::class))
                    return pop.simpleName!!
                clsQueue.addAll(pop.superclasses)
            }
            throw ExceptionInInitializerError("Failed to find type that directly implements Storable")
        }
    }

    fun onRead(entity: Entity) {

    }

    fun onWrite(entity: Entity) {

    }

}