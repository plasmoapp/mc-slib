package su.plo.slib.spigot.command

import com.mojang.brigadier.context.CommandContext
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierContext
import su.plo.slib.api.entity.McEntity
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent
import su.plo.slib.command.AbstractCommandManager
import su.plo.slib.spigot.SpigotServerLib
import su.plo.slib.spigot.nms.ReflectionProxies
import su.plo.slib.spigot.nms.getCommandDispatcher

class SpigotCommandManager(
    private val minecraftServer: SpigotServerLib
) : AbstractCommandManager<McCommand>() {

    private val logger = McLoggerFactory.createLogger("SpigotCommandManager")

    @Synchronized
    fun registerCommands(loader: JavaPlugin) {
        McServerCommandsRegisterEvent.invoker.onCommandsRegister(this, minecraftServer)

        registerCommands { name, command ->
            val spigotCommand = SpigotCommand(minecraftServer, this, command, name)
            val commandMap = loader.commandMap()

            commandMap.register("plasmovoice", spigotCommand)
        }

        try {
            val dispatcher = loader.server.getCommandDispatcher()
            registerBrigadierCommands { command ->
                dispatcher.register(command)
            }
        } catch (e: Exception) {
            logger.warn("Failed to get Brigadier dispatcher: ${e.message}")
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

    override fun <S> getBrigadierContext(context: CommandContext<S>): McBrigadierContext {
        val sourceStack = context.source as Any

        val source = ReflectionProxies.commandSourceStack.getBukkitSender(sourceStack)
            .let { getCommandSource(it) }
        val entity = ReflectionProxies.commandSourceStack.getEntity(sourceStack)
            ?.let { ReflectionProxies.entity.getBukkitEntity(it) }
            ?.let { minecraftServer.getEntityByInstance(it) }

        return BrigadierContext(source, entity)
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

    private data class BrigadierContext(
        override val source: McCommandSource,
        override val executor: McEntity?,
    ): McBrigadierContext
}
