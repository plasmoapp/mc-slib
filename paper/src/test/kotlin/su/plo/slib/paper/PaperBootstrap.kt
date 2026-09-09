@file:Suppress("UnstableApiUsage")

package su.plo.slib.paper

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.paper.command.registerCommandHandler
import su.plo.slib.server.registerCommands

internal val LOGGER = McLoggerFactory.createLogger("slib-paper-test")

class PaperBootstrap : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        registerCommands()
        context.registerCommandHandler(LOGGER, logRegisteredCommands = true)
    }
}
