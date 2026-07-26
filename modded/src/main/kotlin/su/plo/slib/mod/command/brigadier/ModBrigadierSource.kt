package su.plo.slib.mod.command.brigadier

import net.minecraft.commands.CommandSourceStack
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.entity.McEntity
import su.plo.slib.mod.ModServerLib
import su.plo.slib.mod.mixin.accessor.CommandSourceStackAccessor

data class ModBrigadierSource(
    override val source: McCommandSource,
    override val executor: McEntity?,
    override val isSilent: Boolean,
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
            val silent = (sourceStack as CommandSourceStackAccessor).slib_isSilent()

            return ModBrigadierSource(source, executor, silent, sourceStack)
        }
    }
}
