package su.plo.slib.velocity.command

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.command.CommandExecuteEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.proxy.McProxyLib
import su.plo.slib.api.proxy.command.McProxyCommand
import su.plo.slib.api.proxy.event.command.McProxyCommandExecuteEvent
import su.plo.slib.velocity.extension.textConverter

class VelocityCommandManager(
    private val minecraftProxy: McProxyLib
) : McCommandManager<McProxyCommand>() {

    @Subscribe
    fun onCommandExecute(event: CommandExecuteEvent) {
        val command = event.command
        val commandAlias = command.split(" ")
            .dropLastWhile { it.isEmpty() }
            .getOrNull(0) ?: ""

        val commandSource = getCommandSource(event.commandSource)
        McProxyCommandExecuteEvent.invoker.onCommandExecute(commandSource, command)

        val proxyCommand = commandByName[commandAlias] ?: return

        val spaceIndex = command.indexOf(' ')
        val args = if (spaceIndex >= 0) {
            command.substring(spaceIndex + 1).split(" ").toTypedArray()
        } else {
            emptyArray()
        }

        if (proxyCommand.passToBackendServer(commandSource, args)) {
            event.result = CommandExecuteEvent.CommandResult.forwardToServer()
        }
    }

    override fun getCommandSource(source: Any): McCommandSource {
        if (source !is CommandSource)
            throw IllegalArgumentException("source is not " + CommandSource::class.java)

        return if (source is Player) {
            minecraftProxy.getPlayerByInstance(source)
        } else VelocityDefaultCommandSource(minecraftProxy.textConverter(), source)
    }

    @Synchronized
    fun registerCommands(proxyServer: ProxyServer) {
        commandByName.forEach { (name, command) ->
            // todo: group commands and use aliases?
            val velocityCommand = VelocityCommand(minecraftProxy, this, command)
            proxyServer.commandManager.register(name, velocityCommand)
        }
        registered = true
    }
}
