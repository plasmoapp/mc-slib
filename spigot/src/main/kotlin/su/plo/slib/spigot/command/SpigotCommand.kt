package su.plo.slib.spigot.command

import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.McCommand

class SpigotCommand(
    private val commandManager: SpigotCommandManager,
    private val command: McCommand,
    name: String
) : Command(name) {

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
        val source = commandManager.getCommandSource(sender)
        if (!command.hasPermission(source, args)) {
            source.sendMessage(McTextComponent.translatable("pv.error.no_permissions"))
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
    ): List<String> =
        command.suggest(commandManager.getCommandSource(sender), args)

    override fun testPermissionSilent(target: CommandSender): Boolean =
        command.hasPermission(commandManager.getCommandSource(target), null)
}
