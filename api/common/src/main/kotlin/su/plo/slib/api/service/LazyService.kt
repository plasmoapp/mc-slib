package su.plo.slib.api.service

import org.jetbrains.annotations.ApiStatus
import java.util.ServiceLoader

@ApiStatus.Internal
inline fun <reified T> lazyServiceOrNull(): Lazy<T?> =
    lazy {
        // some loaders can't find service by class's classloader,
        // some can't find it by context class loader
        // so we're just trying both
        ServiceLoader.load(T::class.java).firstOrNull()
            ?: ServiceLoader.load(T::class.java, T::class.java.classLoader).firstOrNull()
    }

inline fun <reified T> lazyService(): Lazy<T> {
    val service = lazyServiceOrNull<T>()

    return lazy {
        service.value ?: throw IllegalStateException("${T::class.java} not found is classpath")
    }
}
