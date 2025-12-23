package su.plo.slib.bungee.command

import com.mojang.brigadier.context.CommandContext
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierContext
import su.plo.slib.api.entity.McEntity
import su.plo.slib.api.proxy.command.McProxyCommand
import su.plo.slib.api.proxy.event.command.McProxyCommandExecuteEvent
import su.plo.slib.bungee.BungeeProxyLib
import su.plo.slib.bungee.command.brigadier.BrigadierBungeeCommand
import su.plo.slib.command.AbstractCommandManager

class BungeeCommandManager(
    private val minecraftProxy: BungeeProxyLib
) : AbstractCommandManager<McProxyCommand>(), Listener {

    @EventHandler
    fun onChat(event: ChatEvent) {
        if (!event.isProxyCommand) return

        val command: String = event.message.substringBefore("/")
        val commandSource = getCommandSource(event.sender)
        McProxyCommandExecuteEvent.invoker.onCommandExecute(commandSource, command)
    }

    @Synchronized
    fun registerCommands(plugin: Plugin, proxyServer: ProxyServer) {
        registerCommands { name, command ->
            proxyServer.pluginManager.registerCommand(plugin, BungeeCommand(minecraftProxy, this, command, name))
        }

        registerBrigadierCommands { command ->
            proxyServer.pluginManager.registerCommand(plugin, BrigadierBungeeCommand(this, command))
        }

        registered = true
    }

    override fun <S> getBrigadierContext(context: CommandContext<S>): McBrigadierContext =
        context.source as BrigadierContext

    override fun getCommandSource(source: Any): McCommandSource {
        require(source is CommandSender) { "source is not ${CommandSender::class.java}" }

        return if (source is ProxiedPlayer) {
            minecraftProxy.getPlayerByInstance(source)
        } else BungeeDefaultCommandSource(minecraftProxy, source)
    }

    data class BrigadierContext(
        override val source: McCommandSource,
        override val executor: McEntity? = null,
    ) : McBrigadierContext
}
