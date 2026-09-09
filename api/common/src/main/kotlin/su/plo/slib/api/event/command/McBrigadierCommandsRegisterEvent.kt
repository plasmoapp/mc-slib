package su.plo.slib.api.event.command

import su.plo.slib.api.command.brigadier.McBrigadierRegistry
import su.plo.slib.api.event.GlobalEvent
import su.plo.slib.api.logging.McLoggerFactory

private val logger = McLoggerFactory.createLogger("slib")

/**
 * An event fired every time the server or proxy rebuilds its command dispatcher.
 *
 * Register the listener as early as possible.
 * From `PluginBootstrap` on Paper to register commands in [McBrigadierRegistry.Phase.BOOTSTRAP] phase,
 * or from the constructor of your plugin or mod otherwise.
 */
object McBrigadierCommandsRegisterEvent
    : GlobalEvent<McBrigadierCommandsRegisterEvent.Callback>(
    { callbacks ->
        Callback { registry ->
            callbacks.forEach { callback ->
                try {
                    callback.onCommandsRegister(registry)
                } catch (e: Throwable) {
                    logger.error("Failed to build brigadier commands in {}", callback, e)
                }
            }
        }
    }
) {
    fun interface Callback {
        fun onCommandsRegister(registry: McBrigadierRegistry)
    }
}
