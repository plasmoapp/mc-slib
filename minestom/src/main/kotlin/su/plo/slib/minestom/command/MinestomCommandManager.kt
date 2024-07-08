package su.plo.slib.minestom.command

import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent

class MinestomCommandManager(
    private val minecraftServer: McServerLib
) : McCommandManager<McCommand>() {

    @Synchronized
    fun registerCommands() {
        McServerCommandsRegisterEvent.invoker.onCommandsRegister(this, minecraftServer)

        commandByName.forEach { (name, command) ->
            val cmd = Command(name)
            cmd.setDefaultExecutor { sender, context ->
                val source = getCommandSource(sender)
                val args = context.map.map { it.value.toString() }.toTypedArray()
                if (!command.hasPermission(source, args)) {
                    source.sendMessage(minecraftServer.permissionManager.noPermissionMessage)
                    return@setDefaultExecutor
                }

                command.execute(source, args)
            }
            MinecraftServer.getCommandManager().register(cmd)
        }

        registered = true
    }

    override fun getCommandSource(source: Any): McCommandSource  {
        require(source is CommandSender) { "source is not ${CommandSender::class.java}" }

        return if (source is Player) minecraftServer.getPlayerByInstance(source)
        else MinestomDefaultCommandSource(minecraftServer.textConverter, source)
    }
}
