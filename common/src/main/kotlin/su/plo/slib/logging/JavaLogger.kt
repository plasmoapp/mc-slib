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
        logFormatted(Level.FINE, format, arguments)
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
        if (!isLoggable(level)) return

        val thrown = arguments.lastOrNull() as? Throwable
        val formatArguments = if (thrown != null) arguments.dropLast(1) else arguments.asList()

        log(level, format.formatSlf4j(formatArguments), thrown)
    }

    private fun String.formatSlf4j(arguments: List<Any?>): String {
        val result = StringBuilder(length)
        var start = 0

        for (argument in arguments) {
            val placeholder = indexOf("{}", start)
            if (placeholder < 0) break

            result.append(this, start, placeholder).append(argument)
            start = placeholder + 2
        }

        return result.append(this, start, length).toString()
    }
}
