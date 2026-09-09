package su.plo.slib.mod.command.brigadier

import net.minecraft.commands.CommandSourceStack
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.entity.McEntity
import su.plo.slib.mod.ModServerLib
import su.plo.slib.mod.command.ModUnboundCommandSource
import su.plo.slib.mod.mixin.accessor.CommandSourceStackAccessor

data class ModBrigadierSource(
    override val source: McCommandSource,
    override val executor: McEntity?,
    override val isSilent: Boolean,
    override val isBound: Boolean,
    private val instance: CommandSourceStack,
) : McBrigadierSource {
    @Suppress("UNCHECKED_CAST")
    override fun <T> getInstance(): T =
        instance as T

    companion object {
        fun from(sourceStack: CommandSourceStack): ModBrigadierSource {
            val silent = (sourceStack as CommandSourceStackAccessor).slib_isSilent()

            if (!ModServerLib.isBound) {
                return ModBrigadierSource(ModUnboundCommandSource, null, silent, false, sourceStack)
            }

            val executor = sourceStack.entity?.let { ModServerLib.getEntityByInstance(it) }
            val source = ModServerLib.commandManager.getCommandSource(sourceStack)

            return ModBrigadierSource(source, executor, silent, true, sourceStack)
        }
    }
}
