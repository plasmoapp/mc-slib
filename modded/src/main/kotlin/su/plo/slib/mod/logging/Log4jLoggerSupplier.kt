package su.plo.slib.mod.logging

import su.plo.slib.api.logging.McLogger
import su.plo.slib.api.logging.McLoggerFactory

class Log4jLoggerSupplier : McLoggerFactory.Supplier {
    override fun createLogger(name: String): McLogger =
        Log4jLogger(name)
}
