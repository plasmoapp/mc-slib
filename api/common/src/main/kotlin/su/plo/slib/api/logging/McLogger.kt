package su.plo.slib.api.logging

/**
 * Logger interface for wrapping different loggers.
 */
interface McLogger {

    /**
     * Gets the name of the logger.
     */
    fun getName(): String

    /**
     * Log a message at the TRACE level.
     *
     * @param format    The format string.
     * @param arguments A list of arguments.
     */
    fun trace(format: String, vararg arguments: Any)

    /**
     * Log a message at the DEBUG level.
     *
     * @param format    The format string.
     * @param arguments A list of arguments.
     */
    fun debug(format: String, vararg arguments: Any)

    /**
     * Log a message at the INFO level.
     *
     * @param format    The format string.
     * @param arguments A list of arguments.
     */
    fun info(format: String, vararg arguments: Any)

    /**
     * Log a message at the WARN level.
     *
     * @param format    The format string.
     * @param arguments A list of arguments.
     */
    fun warn(format: String, vararg arguments: Any)

    /**
     * Log a message at the ERROR level.
     *
     * @param format    The format string.
     * @param arguments A list of arguments.
     */
    fun error(format: String, vararg arguments: Any)
}
