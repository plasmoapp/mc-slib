package su.plo.slib.mod.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.entity.player.Player
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.server.McServerLib
import su.plo.slib.command.AbstractCommandManager
import su.plo.slib.mod.mixin.accessor.CommandSourceStackAccessor

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

    override fun getCommandSource(sourceStack: Any): McCommandSource {
        require(sourceStack is CommandSourceStack) { "source is not " + CommandSourceStack::class.java }
        require(sourceStack is CommandSourceStackAccessor) { "source is not " + CommandSourceStack::class.java }

        val source = sourceStack.slib_getSource()

        return if (source is Player) {
            minecraftServer.getPlayerByInstance(source)
        } else ModDefaultCommandSource(minecraftServer, sourceStack)
    }
}
