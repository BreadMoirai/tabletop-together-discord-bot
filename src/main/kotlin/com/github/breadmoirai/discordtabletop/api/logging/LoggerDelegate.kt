package com.github.breadmoirai.discordtabletop.api.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


fun <R : Any> logger() = LoggerDelegate<R>()

@JvmName("loggerNothing")
fun logger() = logger<Nothing>()

class LoggerDelegate<in R : Any> : ReadOnlyProperty<R, ContextAwareLogger> {
    override operator fun getValue(thisRef: R, property: KProperty<*>): ContextAwareLogger {
        val logger = if (thisRef::class.isCompanion) {
            getLogger(thisRef::class.java.enclosingClass)
        } else {
            getLogger(thisRef::class.java)
        }
        return ContextAwareLogger(logger)
    }

    operator fun getValue(nothing: Nothing?, property: KProperty<*>): Logger {
        return getLogger(property.javaClass.name)
    }
}

class ContextAwareLoggerException(override val message: String) : Exception(message)

class ContextAwareLogger(logger: Logger) : Logger by logger {
    companion object {
        private val contexts: MutableMap<Any, ContextStack> = Collections.synchronizedMap(WeakHashMap())
    }

    fun setContext(contextTracker: Any, name: String, parent: Any? = null) {
        if (parent != null) {
            val parentCtx = getContext(parent)
            contexts[contextTracker] = ContextStack(Optional.of(parentCtx), name)
        } else {
            contexts[contextTracker] = ContextStack(Optional.empty(), name)
        }

    }

    private fun getContext(contextTracker: Any): ContextStack {
        return contexts[contextTracker] ?: throw ContextAwareLoggerException("No context for tracker $contextTracker")
    }

    fun trace(contextTracker: Any, msg: String) {
        this.trace("${getContext(contextTracker)}$msg")
    }

    fun debug(contextTracker: Any, msg: String) {
        this.debug("${getContext(contextTracker)}$msg")
    }

    fun info(contextTracker: Any, msg: String) {
        this.info("${getContext(contextTracker)}$msg")
    }

    fun warn(contextTracker: Any, msg: String) {
        this.warn("${getContext(contextTracker)}$msg")
    }

    fun error(contextTracker: Any, msg: String) {
        this.error("${getContext(contextTracker)}$msg")
    }

    class ContextStack(val parent: Optional<ContextStack>, val name: String) {
        companion object {
            var formatString = "[%s]"
            var separator = ""

        }

        override fun toString(): String {
            return if (parent.isPresent) {
                "${parent.get()}$separator${String.format(formatString, name)} "
            } else {
                "${String.format(formatString, name)} "
            }
        }
    }
}