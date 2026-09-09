@file:Suppress("UnstableApiUsage")

package su.plo.slib.paper.command

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent
import su.plo.slib.command.AbstractCommandManager
import su.plo.slib.paper.PaperServerLib
import su.plo.slib.paper.command.brigadier.collectAndApply

class PaperCommandManager(
    private val minecraftServer: PaperServerLib,
) : AbstractCommandManager<McCommand>(minecraftServer.baseLogger) {
    @Synchronized
    fun registerCommands(loader: JavaPlugin) {
        McServerCommandsRegisterEvent.invoker.onCommandsRegister(this, minecraftServer)
        registered = true

        loader.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val registrar = event.registrar()

            try {
                registrar.collectAndApply(logger, logRegisteredCommands)
                registerBasicCommands(registrar)
            } catch (e: Throwable) {
                logger.error("Failed to register commands", e)
            }
        }
    }

    override fun getCommandSource(source: Any): McCommandSource {
        require(source is CommandSender) { "source is not ${CommandSender::class.java}" }

        return if (source is Player) minecraftServer.getPlayerByInstance(source)
        else PaperDefaultCommandSource(minecraftServer, source)
    }

    @Synchronized
    private fun registerBasicCommands(registrar: Commands) {
        registerCommands { name, command, _ ->
            try {
                registrar.register(
                    name,
                    null,
                    emptyList(),
                    PaperBasicCommand(minecraftServer, this, command),
                )
            } catch (e: Throwable) {
                logger.error("Failed to register command '{}'", name, e)
            }
        }
    }
}
