package su.plo.slib.api.event.command

import su.plo.slib.api.command.brigadier.McBrigadierRegistry
import su.plo.slib.api.event.GlobalEvent
import su.plo.slib.api.logging.McLoggerFactory

private val logger = McLoggerFactory.createLogger("slib")

/**
 * An event fired every time the server or proxy rebuilds its command dispatcher.
 *
 * Register the listener as early as possible:
 * from `PluginBootstrap` on Paper, or from the constructor of your plugin or mod on other platforms.
 *
 * On Paper, once any plugin registers the bootstrap command handler, the startup dispatcher is built
 * in [McBrigadierRegistry.Phase.BOOTSTRAP], before any `JavaPlugin` exists.
 * A listener registered from the plugin constructor, `onLoad` or `onEnable` is then only called on `/minecraft:reload`.
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
