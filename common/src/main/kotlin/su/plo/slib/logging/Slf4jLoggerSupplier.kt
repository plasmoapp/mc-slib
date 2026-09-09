package su.plo.slib.logging

import su.plo.slib.api.logging.McLogger
import su.plo.slib.api.logging.McLoggerFactory

class Slf4jLoggerSupplier : McLoggerFactory.Supplier {
    override fun createLogger(name: String): McLogger =
        Slf4jLogger(name)
}
