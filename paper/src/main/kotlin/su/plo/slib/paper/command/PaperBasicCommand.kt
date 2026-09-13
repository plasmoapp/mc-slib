@file:Suppress("UnstableApiUsage")

package su.plo.slib.paper.command

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.server.McServerLib

class PaperBasicCommand(
    private val minecraftServer: McServerLib,
    private val commandManager: PaperCommandManager,
    private val command: McCommand,
) : BasicCommand {
    override fun execute(commandSourceStack: CommandSourceStack, args: Array<String>) {
        val source = commandManager.getCommandSource(commandSourceStack.sender)

        if (!command.hasPermission(source, args)) {
            source.sendMessage(minecraftServer.permissionManager.noPermissionMessage)
            return
        }

        command.execute(source, args)
    }

    override fun suggest(commandSourceStack: CommandSourceStack, args: Array<String>): Collection<String> =
        command.suggest(commandManager.getCommandSource(commandSourceStack.sender), args)

    override fun canUse(sender: CommandSender): Boolean =
        command.hasPermission(commandManager.getCommandSource(sender), null)
}
