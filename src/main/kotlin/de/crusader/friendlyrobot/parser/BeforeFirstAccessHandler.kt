package de.crusader.friendlyrobot.parser

import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Allows to run a task before first access to the provided value
 */
internal class BeforeFirstAccessHandler<T>(
        val value: T,
        val firstAccessHandler: () -> Unit
) : ReadOnlyProperty<Any?, T> {

    /**
     * Atomic boolean to determinate if value was already accessed
     */
    private val alreadyAccessed = AtomicBoolean(false)

    /**
     * Checks if already accessed to the value and provides
     * this value after first initialization
     */
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (!alreadyAccessed.getAndSet(true)) {
            firstAccessHandler()
        }
        return value
    }

}