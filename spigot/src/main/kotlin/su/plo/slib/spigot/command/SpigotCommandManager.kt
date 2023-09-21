package su.plo.slib.spigot.command

import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.spigot.extension.textConverter

class SpigotCommandManager(
    private val minecraftServer: McServerLib
) : McCommandManager<McCommand>() {

    @Synchronized
    fun registerCommands(loader: JavaPlugin) {
        commandByName.forEach { (name, command) ->
            val spigotCommand = SpigotCommand(this, command, name)

            val commandMap = loader.server.javaClass
                .getDeclaredField("commandMap").also {
                    it.isAccessible = true
                }
                .get(loader.server) as SimpleCommandMap

            commandMap.register("plasmovoice", spigotCommand)
        }

        registered = true
    }

    override fun getCommandSource(source: Any): McCommandSource  {
        require(source is CommandSender) { "source is not ${CommandSender::class.java}" }

        return if (source is Player) minecraftServer.getPlayerByInstance(source)
        else SpigotDefaultCommandSource(minecraftServer.textConverter(), source)
    }
}
