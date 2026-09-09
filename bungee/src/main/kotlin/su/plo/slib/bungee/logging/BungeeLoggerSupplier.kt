package su.plo.slib.bungee.logging

import net.md_5.bungee.api.ProxyServer
import su.plo.slib.api.logging.McLogger
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.logging.JavaLogger

class BungeeLoggerSupplier : McLoggerFactory.Supplier {
    override fun createLogger(name: String): McLogger =
        JavaLogger(name).apply { parent = ProxyServer.getInstance().logger }
}
