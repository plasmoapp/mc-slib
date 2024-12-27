package su.plo.slib.spigot.command

import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.server.McServerLib

class SpigotCommand(
    private val minecraftServer: McServerLib,
    private val commandManager: SpigotCommandManager,
    private val command: McCommand,
    name: String
) : Command(name) {

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
        if (!isRegistered) {
            return false
        }

        val source = commandManager.getCommandSource(sender)
        if (!command.hasPermission(source, args)) {
            source.sendMessage(minecraftServer.permissionManager.noPermissionMessage)
            return true
        }

        command.execute(source, args)
        return true
    }

    @Throws(IllegalArgumentException::class)
    override fun tabComplete(
        sender: CommandSender,
        alias: String,
        args: Array<String>,
        location: Location?
    ): List<String> {
        if (!isRegistered) {
            return super.tabComplete(sender, alias, args)
        }

        return command.suggest(commandManager.getCommandSource(sender), args)
    }

    override fun testPermissionSilent(target: CommandSender): Boolean {
        if (!isRegistered) {
            return super.testPermissionSilent(target)
        }

        return command.hasPermission(commandManager.getCommandSource(target), null)
    }
}
