package su.plo.slib.api.logging

class McLazyLogger(
    private val nameResolver: NameResolver,
) : McLogger {
    constructor(name: String) : this(NameResolver { name })

    @Deprecated("Binary compatibility", level = DeprecationLevel.HIDDEN)
    constructor(nameResolver: (McLazyLogger) -> String) : this(NameResolver { nameResolver(it) })

    private var logger: McLogger? = null

    private val resolvedName by lazy { nameResolver.resolve(this) }

    val value: McLogger
        get() = logger ?: McLoggerFactory.supplier.createLogger(resolvedName)

    fun isInitialized(): Boolean =
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

    fun interface NameResolver {
        fun resolve(logger: McLazyLogger): String
    }
}
