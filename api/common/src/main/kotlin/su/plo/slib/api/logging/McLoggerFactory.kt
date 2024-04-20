package su.plo.slib.api.logging

import org.jetbrains.annotations.ApiStatus.Internal

object McLoggerFactory {

    /**
     * Creates a logger.
     *
     * Usually loggers are used in static fields,
     * so there is a possibility that the supplier is not initialized yet.
     * Because of that, we're using lazy initialization here.
     * This way, the logger will only be initialized on access when the supplier is (hopefully) initialized.
     */
    @JvmStatic
    fun createLogger(name: String): McLazyLogger =
        McLazyLogger(name)

    @Internal
    lateinit var supplier: Supplier

    fun interface Supplier {

        fun createLogger(name: String): McLogger
    }
}
