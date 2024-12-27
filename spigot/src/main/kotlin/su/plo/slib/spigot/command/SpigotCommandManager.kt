package su.plo.slib.spigot.command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent
import su.plo.slib.spigot.SpigotServerLib

class SpigotCommandManager(
    private val minecraftServer: SpigotServerLib
) : McCommandManager<McCommand>() {

    private val logger = McLoggerFactory.createLogger("SpigotCommandManager")

    @Synchronized
    fun registerCommands(loader: JavaPlugin) {
        McServerCommandsRegisterEvent.invoker.onCommandsRegister(this, minecraftServer)

        commandByName.forEach { (name, command) ->
            val spigotCommand = SpigotCommand(minecraftServer, this, command, name)
            val commandMap = loader.commandMap()

            commandMap.register("plasmovoice", spigotCommand)
        }

        registered = true
    }

    @Synchronized
    fun clear(loader: JavaPlugin) {
        val commandMap = loader.commandMap()

        commandByName.keys
            .mapNotNull { commandMap.getCommand(it) }
            .forEach { commandMap.unregister(it) }

        super.clear()
    }

    override fun getCommandSource(source: Any): McCommandSource  {
        require(source is CommandSender) { "source is not ${CommandSender::class.java}" }

        return if (source is Player) minecraftServer.getPlayerByInstance(source)
        else SpigotDefaultCommandSource(minecraftServer, source)
    }

    @Suppress("UNCHECKED_CAST")
    private fun SimpleCommandMap.unregister(command: Command) {
        try {
            command.unregister(this)

            val knownCommandsField = SimpleCommandMap::class.java.getDeclaredField("knownCommands")
            knownCommandsField.isAccessible = true

            val knownCommands = knownCommandsField.get(this) as MutableMap<String, Command>
            knownCommands.remove(command.name)
        } catch (e: Throwable) {
            logger.error("Failed to unregister command ${command.name}", e)
        }
    }

    private fun JavaPlugin.commandMap(): SimpleCommandMap =
        server.javaClass
            .getDeclaredField("commandMap")
            .also { it.isAccessible = true }
            .get(server) as SimpleCommandMap
}
