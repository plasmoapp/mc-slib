package su.plo.slib.api.event

import java.util.Collections
import java.util.concurrent.CopyOnWriteArraySet
import java.util.function.Function

abstract class GlobalEvent<T> (
    invokerSupplier: Function<Collection<T>, T>
) {
    private val registeredListeners: MutableSet<T> = CopyOnWriteArraySet()

    /**
     * A read-only view of the currently registered listeners.
     */
    val listeners: Collection<T> = Collections.unmodifiableSet(registeredListeners)

    val invoker = invokerSupplier.apply(registeredListeners)

    fun registerListener(listener: T) {
        registeredListeners.add(listener)
    }

    fun unregisterListener(listener: T) {
        registeredListeners.remove(listener)
    }

    fun clearListeners() {
        registeredListeners.clear()
    }
}
