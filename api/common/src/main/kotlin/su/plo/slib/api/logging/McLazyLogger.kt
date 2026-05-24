package su.plo.slib.api.logging

class McLazyLogger(
    private val nameResolver: (McLazyLogger) -> String
) : McLogger, Lazy<McLogger> {

    constructor(name: String) : this({ _ -> name })

    private var logger: McLogger? = null

    private val resolvedName by lazy { nameResolver(this) }

    override val value: McLogger
        get() = logger ?: McLoggerFactory.supplier.createLogger(resolvedName)

    override fun isInitialized(): Boolean =
        logger != null

    override fun getName(): String = resolvedName

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
