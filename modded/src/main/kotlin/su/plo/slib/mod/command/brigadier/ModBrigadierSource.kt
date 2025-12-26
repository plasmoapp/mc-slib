package su.plo.slib.mod.command.brigadier

import net.minecraft.commands.CommandSourceStack
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.entity.McEntity
import su.plo.slib.mod.ModServerLib

data class ModBrigadierSource(
    override val source: McCommandSource,
    override val executor: McEntity?,
    private val instance: CommandSourceStack,
) : McBrigadierSource {

    @Suppress("UNCHECKED_CAST")
    override fun <T> getInstance(): T =
        instance as T

    companion object {
        private val minecraftServer by lazy { ModServerLib }

        fun from(sourceStack: CommandSourceStack): ModBrigadierSource {
            val executor = sourceStack.entity?.let { minecraftServer.getEntityByInstance(it) }

            val source = minecraftServer.commandManager.getCommandSource(sourceStack)

            return ModBrigadierSource(source, executor, sourceStack)
        }
    }
}
