@file:Suppress("UnstableApiUsage")

package su.plo.slib.paper.command

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.server.McServerLib

class PaperBasicCommand internal constructor(
    private val minecraftServer: McServerLib,
    private val commandManager: PaperCommandManager,
    private val command: McCommand,
    private val binding: PaperPluginBinding,
) : BasicCommand {
    override fun execute(commandSourceStack: CommandSourceStack, args: Array<String>) {
        if (binding.isUnbound()) return

        val source = commandManager.getCommandSource(commandSourceStack.sender)

        if (!command.hasPermission(source, args)) {
            source.sendMessage(minecraftServer.permissionManager.noPermissionMessage)
            return
        }

        command.execute(source, args)
    }

    override fun suggest(commandSourceStack: CommandSourceStack, args: Array<String>): Collection<String> =
        if (binding.isUnbound()) emptyList()
        else command.suggest(commandManager.getCommandSource(commandSourceStack.sender), args)

    override fun canUse(sender: CommandSender): Boolean =
        !binding.isUnbound() && command.hasPermission(commandManager.getCommandSource(sender), null)
}
