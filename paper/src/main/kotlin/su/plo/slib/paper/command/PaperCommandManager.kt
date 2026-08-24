package su.plo.slib.paper.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent
import su.plo.slib.command.AbstractCommandManager
import su.plo.slib.command.copyFor
import su.plo.slib.command.proxied
import su.plo.slib.paper.PaperServerLib
import su.plo.slib.paper.command.brigadier.PaperBrigadierSource
import su.plo.slib.paper.nms.getCommandDispatcher

class PaperCommandManager(
    private val minecraftServer: PaperServerLib
) : AbstractCommandManager<McCommand>(minecraftServer.baseLogger) {

    private val logger = McLoggerFactory.createLogger(minecraftServer.baseLogger, javaClass.simpleName)

    @Synchronized
    fun registerCommands(loader: JavaPlugin) {
        McServerCommandsRegisterEvent.invoker.onCommandsRegister(this, minecraftServer)

        registerCommands { name, command, namespace ->
            val paperCommand = PaperCommand(minecraftServer, this, command, name)
            val commandMap = loader.commandMap()

            commandMap.register(namespace, paperCommand)
        }

        applyBrigadierCommands(loader)
        registerPaperCommandLifecycleEvent(loader, logger) { applyBrigadierCommands(loader) }

        registered = true
    }

    @Synchronized
    private fun applyBrigadierCommands(loader: JavaPlugin) {
        try {
            val dispatcher = loader.server.getCommandDispatcher()
            registerBrigadierCommands { command, namespace ->
                val proxied = command.proxied(
                    PaperBrigadierSource::from,
                    { it.toMc() },
                )

                dispatcher.root.addChild(proxied)
                dispatcher.root.addChild(proxied.copy("$namespace:${command.literal}"))
            }
        } catch (e: Exception) {
            logger.warn("Failed to get Brigadier dispatcher", e)
        }
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
        else PaperDefaultCommandSource(minecraftServer, source)
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

private fun <S> LiteralCommandNode<S>.copy(newLiteral: String): LiteralCommandNode<S> {
    val builder = LiteralArgumentBuilder.literal<S>(newLiteral)
        .requires(requirement)
        .forward(redirect, redirectModifier, isFork)

    command?.let { builder.executes(it) }
    children.forEach { builder.then(it) }

    return builder.build()
}

fun CommandContext<McBrigadierSource>.toSourceStack(): CommandContext<Any> =
    copyFor<McBrigadierSource, Any>(source.getInstance())

fun CommandContext<Any>.toMc(): CommandContext<McBrigadierSource> =
    copyFor<Any, McBrigadierSource>(PaperBrigadierSource.from(source))

