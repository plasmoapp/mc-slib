package su.plo.slib.mod.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.entity.player.Player
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierContext
import su.plo.slib.api.entity.McEntity
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

    override fun <S> getBrigadierContext(context: CommandContext<S>): McBrigadierContext {
        val sourceStack = context.source
        require(sourceStack is CommandSourceStack) { "source is not " + CommandSourceStack::class.java }

        val executor = sourceStack.entity?.let {
            if (it is Player) {
                minecraftServer.getPlayerByInstance(it)
            } else {
                minecraftServer.getEntityByInstance(it)
            }
        }

        // todo: sourceStack.source is not accessible
//        val source =
//            if (sourceStack.source is Player) {
//                minecraftServer.getPlayerByInstance(entity)
//            } else ModDefaultCommandSource(minecraftServer, sourceStack)
        val source = getCommandSource(sourceStack)

        return BrigadierContext(getCommandSource(source), executor)
    }

    override fun getCommandSource(source: Any): McCommandSource {
        require(source is CommandSourceStack) { "source is not " + CommandSourceStack::class.java }

        val entity = source.entity

        return if (entity is Player) {
            minecraftServer.getPlayerByInstance(entity)
        } else ModDefaultCommandSource(minecraftServer, source)
    }

    private data class BrigadierContext(
        override val source: McCommandSource,
        override val executor: McEntity?
    ) : McBrigadierContext
}
