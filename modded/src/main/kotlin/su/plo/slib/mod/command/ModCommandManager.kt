package su.plo.slib.mod.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.entity.player.Player
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.server.McServerLib
import su.plo.slib.command.AbstractCommandManager

class ModCommandManager(
    private val minecraftServer: McServerLib
) : AbstractCommandManager<McCommand>() {

    @Synchronized
    fun registerCommands(dispatcher: CommandDispatcher<CommandSourceStack>) {
        registerCommands { name, command ->
            val modCommand = ModCommand(minecraftServer, this, command)
            modCommand.register(dispatcher, name)
        }

        @Suppress("UNCHECKED_CAST")
        registerBrigadierCommands { command ->
            dispatcher.register(command as LiteralArgumentBuilder<CommandSourceStack>)
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
