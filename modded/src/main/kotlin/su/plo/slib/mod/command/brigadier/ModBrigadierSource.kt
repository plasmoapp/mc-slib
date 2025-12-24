package su.plo.slib.mod.command.brigadier

import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
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

        val executor = sourceStack.entity?.let { minecraftServer.getEntityByInstance(it) }

        val source = minecraftServer.commandManager.getCommandSource(sourceStack)

        return ModBrigadierSource(source, executor)
    }
}
