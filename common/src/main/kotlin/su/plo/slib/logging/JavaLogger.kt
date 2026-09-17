package su.plo.slib.logging

import su.plo.slib.api.logging.McLogger
import java.util.logging.Level
import java.util.logging.Logger

class JavaLogger(
    name: String
) : Logger(name, null), McLogger {

    override fun trace(format: String, vararg arguments: Any?) {
        logFormatted(Level.FINEST, format, arguments)
    }

    override fun debug(format: String, vararg arguments: Any?) {
        logFormatted(Level.ALL, format, arguments)
    }

    override fun info(format: String, vararg arguments: Any?) {
        logFormatted(Level.INFO, format, arguments)
    }

    override fun warn(format: String, vararg arguments: Any?) {
        logFormatted(Level.WARNING, format, arguments)
    }

    override fun error(format: String, vararg arguments: Any?) {
        logFormatted(Level.SEVERE, format, arguments)
    }

    private fun logFormatted(level: Level, format: String, arguments: Array<out Any?>) {
        val thrown = arguments.lastOrNull() as? Throwable
        log(level, String.format(format.convertFromSlf4jFormat(), *arguments), thrown)
    }

    private fun String.convertFromSlf4jFormat(): String =
        replace("{}", "%s")
}
