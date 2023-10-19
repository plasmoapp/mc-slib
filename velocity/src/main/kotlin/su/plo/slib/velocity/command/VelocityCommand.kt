package su.plo.slib.velocity.command

import com.velocitypowered.api.command.SimpleCommand
import su.plo.slib.api.proxy.McProxyLib
import su.plo.slib.api.proxy.command.McProxyCommand

class VelocityCommand(
    private val minecraftProxy: McProxyLib,
    private val commandManager: VelocityCommandManager,
    private val command: McProxyCommand
) : SimpleCommand {

    override fun execute(invocation: SimpleCommand.Invocation) {
        val source = commandManager.getCommandSource(invocation.source())
        if (!command.hasPermission(source, invocation.arguments())) {
            source.sendMessage(minecraftProxy.permissionManager.noPermissionMessage)
            return
        }

        command.execute(source, invocation.arguments())
    }

    override fun suggest(invocation: SimpleCommand.Invocation) =
        command.suggest(commandManager.getCommandSource(invocation.source()), invocation.arguments())

    override fun hasPermission(invocation: SimpleCommand.Invocation) =
        command.hasPermission(commandManager.getCommandSource(invocation.source()), null)
}
