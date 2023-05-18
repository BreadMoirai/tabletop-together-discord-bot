package com.github.breadmoirai.discordtabletop.storage

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import com.github.breadmoirai.discordtabletop.storage.Storable.Companion.storableType
import io.ktor.utils.io.*
import jetbrains.exodus.bindings.ComparableBinding
import jetbrains.exodus.bindings.ComparableSet
import jetbrains.exodus.entitystore.Entity
import jetbrains.exodus.entitystore.PersistentEntityStore
import jetbrains.exodus.entitystore.PersistentEntityStores
import jetbrains.exodus.entitystore.StoreTransaction
import jetbrains.exodus.util.LightOutputStream
import java.io.ByteArrayInputStream
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

object Storage {
    private val storeDir = Paths.get("").resolve("discordtabletop-store").createDirectories().toFile()
    val store: PersistentEntityStore = PersistentEntityStores.newInstance(storeDir);

    inline fun <reified T : Comparable<T>> registerPropertyType(
        crossinline read: (ByteArrayInputStream) -> T, crossinline write: (LightOutputStream, T) -> Unit
    ) {
        store.executeInTransaction { txn ->
            store.registerCustomPropertyType(txn, T::class.java, object : ComparableBinding() {
                override fun readObject(stream: ByteArrayInputStream): Comparable<Nothing> {
                    return read(stream)
                }

                override fun writeObject(output: LightOutputStream, obj: Comparable<Nothing>) {
                    write(output, obj as T)
                }
            })
        }
    }

    inline fun <reified T : Storable> read(storableId: Pair<String, Comparable<*>>): Option<T> {
        val store = Storage.store
        return store.computeInTransaction { txn ->
            val storableType = storableType(T::class)
            val find = txn.find(storableType, storableId.first, storableId.second)
            if (find.isEmpty) None
            else read(txn, find.single(), T::class).some()
        }
    }

    fun <T : Any> read(txn: StoreTransaction, entity: Entity, cls: KClass<T>): T {
        val constructor = cls.primaryConstructor!!
        val parameters = constructor.valueParameters
        val args = parameters.associateWith { parameter ->
            val name = parameter.name!!
            if (parameter.type.isSubtypeOf(Storable::class.createType())) {
                read(txn, entity.getLink(name)!!, parameter.type.jvmErasure)
            } else if (parameter.type.isSubtypeOf(Storable::class.createType().withNullability(true))) {
                val link = entity.getLink(name)
                if (link == null) null
                else read(txn, link, parameter.type.jvmErasure)
            } else if (parameter.type.isSubtypeOf(Comparable::class.createType(listOf(KTypeProjection.STAR)))) {
//                println("Reading $name as ${entity.getProperty(name)}")
                // Boolean false values are not stored and are simply absent
                if (parameter.type.isSubtypeOf(Boolean::class.createType())) entity.getProperty(name) == true
                else if (parameter.type.isSubtypeOf(Int::class.createType())) entity.getProperty(name) ?: 0
                else entity.getProperty(name)!!
            } else if (parameter.type.isSubtypeOf(List::class.createType(listOf(KTypeProjection.STAR)))) {
                if (parameter.type.arguments.single().type!!.isSubtypeOf(Storable::class.createType())) {
                    val set = entity.getLinks(name)
                    set.map { read(txn, it, parameter.type.arguments.single().type!!.jvmErasure) }
                } else if (parameter.type.arguments.single().type!!.isSubtypeOf(
                        Comparable::class.createType(
                            listOf(
                                KTypeProjection.STAR
                            )
                        )
                    )
                ) {
                    val set = entity.getProperty(name)!! as ComparableSet<*>
                    set.toList()
                } else {
                    throw Exception(
                        "Failed to read ${this::class.simpleName}. " + "Could not handle property ${parameter.name} with type ${parameter.type.classifier}<${parameter.type.arguments}>."
                    )
                }
            } else {
                throw Exception(
                    "Failed to read ${this::class.simpleName}. " + "Could not handle property ${parameter.name} with type ${parameter.type}."
                )
            }
        }
        val result = constructor.callBy(args)
        (result as Storable).onRead(entity)
        return result
    }

    fun write(obj: Storable): Entity {
        return store.computeInTransaction { txn -> write(txn, obj) }
    }

    fun write(txn: StoreTransaction, obj: Storable): Entity {
        val (storableIdName, storableIdValue) = obj.storableId
        val existing = txn.find(obj.storableType, storableIdName, storableIdValue).firstOrNull()
        val entity = existing ?: txn.newEntity(obj.storableType)
        @Suppress("UNCHECKED_CAST") for (property in (obj::class as KClass<Any>).memberProperties) {
            if (property.hasAnnotation<StorableTransient>()) continue
            if (property.returnType.isSubtypeOf(Storable::class.createType())) {
                entity.setLink(property.name, write(txn, (property.get(obj) as Storable)))
                continue
            }
            if (property.returnType.isSubtypeOf(Storable::class.createType().withNullability(true))) {
                val value = property.get(obj) as Storable?
                if (value == null) entity.deleteLinks(property.name)
                else entity.setLink(property.name, write(txn, (value)))
                continue
            }
            if (property.returnType.isSubtypeOf(Comparable::class.createType(listOf(KTypeProjection.STAR)))) {
//                println("Setting property ${property.name} to ${property.get(obj)} for $storableIdValue")
                entity.setProperty(property.name, property.get(obj) as Comparable<*>)
                continue
            }
            if (property.returnType.isSubtypeOf(List::class.createType(listOf(KTypeProjection.STAR)))) {
                val list = (property.get(obj) as List<*>)
                if (list.all { it is Storable }) {
                    val values = list.map { value -> write(txn, (value as Storable)) }
                    val links = entity.getLinks(property.name)
                    val toRemove = links.filter { link -> values.none { value -> value.id == link.id } }
                    for (remove in toRemove) {
                        entity.deleteLink(property.name, remove)
                    }
                    for (value in values) {
                        entity.addLink(property.name, value)
                    }
                    continue
                } else if (list.all { it is Comparable<*> }) {
                    val values = ComparableSet<Comparable<Any>>()
                    for (any in list) {
                        values.addItem(any as Comparable<Any>)
                    }
                    entity.setProperty(property.name, values)
                    continue
                }
            }
            throw Exception(
                "Failed to write ${obj::class.simpleName} (${obj.storableId}). " + "Could not handle property ${property.name} with type ${property.returnType}."
            )
        }
        obj.onWrite(entity)
        return entity
    }

}