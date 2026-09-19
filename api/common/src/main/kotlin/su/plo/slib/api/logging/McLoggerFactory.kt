package su.plo.slib.api.logging

import su.plo.slib.api.service.lazyService

object McLoggerFactory {
    private val platformSupplier: Supplier by lazyService()

    @Volatile
    private var overriddenSupplier: Supplier? = null

    /**
     * Creates a logger.
     *
     * Usually loggers are used in static fields,
     * so there is a possibility that the supplier is not initialized yet.
     * Because of that, we're using lazy initialization here.
     * This way, the logger will only be initialized on access when the supplier is (hopefully) initialized.
     *
     * @param name The logger name.
     */
    @JvmStatic
    fun createLogger(name: String): McLazyLogger =
        McLazyLogger(name)

    /**
     * Creates a logger named `baseLoggerName/name`.
     *
     * @param baseLogger The logger to prefix the name with.
     * @param name The logger name.
     */
    @JvmStatic
    fun createLogger(baseLogger: McLogger, name: String): McLazyLogger =
        McLazyLogger { "${baseLogger.getName()}/$name" }

    /**
     * The supplier used to create loggers:
     * the one passed to [overrideSupplier] or, if there is none,
     * the platform supplier loaded from the classpath.
     */
    @JvmStatic
    val supplier: Supplier
        get() = overriddenSupplier ?: platformSupplier

    /**
     * Overrides the platform supplier.
     *
     * Loggers resolve the supplier on each use,
     * so the override is applied to the loggers created before this call as well.
     *
     * @param supplier The supplier to use, or `null` to restore the platform supplier.
     */
    @JvmStatic
    fun overrideSupplier(supplier: Supplier?) {
        overriddenSupplier = supplier
    }

    fun interface Supplier {
        /**
         * Creates a logger with the given name.
         *
         * @param name The logger name.
         */
        fun createLogger(name: String): McLogger
    }
}
