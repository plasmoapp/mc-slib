package su.plo.slib.api.logging

class McLazyLogger(
    private val name: String
) : McLogger, Lazy<McLogger> {

    private var logger: McLogger? = null

    override val value: McLogger
        get() = logger ?: McLoggerFactory.supplier.createLogger(name)

    override fun isInitialized(): Boolean =
        logger != null

    override fun getName(): String =
        value.getName()

    override fun trace(format: String, vararg arguments: Any?) =
        value.trace(format, *arguments)

    override fun debug(format: String, vararg arguments: Any?) =
        value.debug(format, *arguments)

    override fun info(format: String, vararg arguments: Any?) =
        value.info(format, *arguments)

    override fun warn(format: String, vararg arguments: Any?) =
        value.warn(format, *arguments)

    override fun error(format: String, vararg arguments: Any?) =
        value.error(format, *arguments)
}
