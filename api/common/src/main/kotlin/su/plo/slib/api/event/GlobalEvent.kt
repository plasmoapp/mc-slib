package su.plo.slib.api.event

import java.util.concurrent.CopyOnWriteArraySet
import java.util.function.Function

abstract class GlobalEvent<T> (
    private val invokerSupplier: Function<Collection<T>, T>
) {
    private val listeners: MutableSet<T> = CopyOnWriteArraySet()

    val invoker = invokerSupplier.apply(listeners)

    fun registerListener(listener: T) {
        if (listeners.add(listener)) invokerSupplier.apply(listeners)
    }

    fun unregisterListener(listener: T) {
        if (listeners.remove(listener)) invokerSupplier.apply(listeners)
    }

    fun clearListeners() {
        listeners.clear()
    }
}
