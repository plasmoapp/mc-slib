package su.plo.slib.bungee.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor
import su.plo.slib.api.proxy.McProxyLib
import su.plo.slib.api.proxy.command.McProxyCommand

class BungeeCommand(
    private val minecraftProxy: McProxyLib,
    private val commandManager: BungeeCommandManager,
    private val command: McProxyCommand,
    name: String
) : Command(name), TabExecutor {

    override fun execute(sender: CommandSender, arguments: Array<String>) {
        val source = commandManager.getCommandSource(sender)

        if (!command.hasPermission(source, arguments)) {
            source.sendMessage(minecraftProxy.permissionManager.noPermissionMessage)
            return
        }

        command.execute(source, arguments)
    }

    override fun onTabComplete(sender: CommandSender, arguments: Array<String>): List<String> =
        command.suggest(commandManager.getCommandSource(sender), arguments)

    override fun hasPermission(sender: CommandSender) =
        command.hasPermission(commandManager.getCommandSource(sender), null)
}
