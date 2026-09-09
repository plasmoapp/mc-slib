@file:Suppress("UnstableApiUsage")

package su.plo.slib.paper.command

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import su.plo.slib.api.logging.McLogger
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.paper.PaperServerLib
import su.plo.slib.paper.command.brigadier.collectAndApply
import java.util.concurrent.atomic.AtomicBoolean

private val registered = AtomicBoolean()

internal val isCommandHandlerBootstrapped: Boolean
    get() = registered.get()

@JvmOverloads
fun BootstrapContext.registerCommandHandler(
    logger: McLogger,
    logRegisteredCommands: Boolean = false,
) {
    if (!registered.compareAndSet(false, true)) return

    try {
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            try {
                event.registrar().collectAndApply(
                    McLoggerFactory.createLogger(logger, "CommandManager"),
                    PaperServerLib.instanceOrNull?.commandManager?.logRegisteredCommands ?: logRegisteredCommands,
                )
            } catch (e: Throwable) {
                logger.error("Failed to register brigadier commands at bootstrap", e)
            }
        }
    } catch (e: Throwable) {
        registered.set(false)

        runCatching {
            logger.warn(
                "Failed to hook into the paper commands bootstrap, commands will not be available in datapacks",
                e,
            )
        }
    }
}
