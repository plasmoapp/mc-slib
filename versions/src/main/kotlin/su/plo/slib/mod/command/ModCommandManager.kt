package su.plo.slib.mod.command

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.entity.player.Player
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.command.McCommandSource

class ModCommandManager(
    private val minecraftServer: McServerLib
) : McCommandManager<McCommand>() {

    @Synchronized
    fun registerCommands(dispatcher: CommandDispatcher<CommandSourceStack>) {
        commandByName.forEach { (name, command) ->
            val modCommand = ModCommand(minecraftServer, this, command)
            modCommand.register(dispatcher, name)
        }
        this.registered = true
    }

    override fun getCommandSource(source: Any): McCommandSource {
        require(source is CommandSourceStack) { "source is not " + CommandSourceStack::class.java }

        val entity = source.entity

        return if (entity is Player) {
            minecraftServer.getPlayerByInstance(entity)
        } else ModDefaultCommandSource(minecraftServer, source)
    }
}
