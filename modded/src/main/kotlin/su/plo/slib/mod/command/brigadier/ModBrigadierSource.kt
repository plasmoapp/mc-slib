package su.plo.slib.mod.command.brigadier

import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.entity.player.Player
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.entity.McEntity
import su.plo.slib.mod.ModServerLib

data class ModBrigadierSource(
    override val source: McCommandSource,
    override val executor: McEntity?
) : McBrigadierSource

class ModBrigadierSourceProvider : McBrigadierSource.Provider {
    private val minecraftServer by lazy { ModServerLib }

    override fun <S> getBrigadierSource(context: CommandContext<S>): McBrigadierSource {
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
        val source = minecraftServer.commandManager.getCommandSource(sourceStack)

        return ModBrigadierSource(minecraftServer.commandManager.getCommandSource(source), executor)
    }
}
