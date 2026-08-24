package su.plo.slib.paper.command.brigadier

import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.entity.McEntity
import su.plo.slib.paper.PaperServerLib
import su.plo.slib.paper.nms.ReflectionProxies

data class PaperBrigadierSource(
    override val source: McCommandSource,
    override val executor: McEntity?,
    override val isSilent: Boolean,
    private val instance: Any,
): McBrigadierSource {

    @Suppress("UNCHECKED_CAST")
    override fun <T> getInstance(): T =
        instance as T

    companion object {
        private val minecraftServer by lazy { PaperServerLib.instance }

        fun from(sourceStack: Any): PaperBrigadierSource {
            val source = ReflectionProxies.commandSourceStack.getBukkitSender(sourceStack)
                .let { minecraftServer.commandManager.getCommandSource(it) }
            val entity = ReflectionProxies.commandSourceStack.getEntity(sourceStack)
                ?.let { ReflectionProxies.entity.getBukkitEntity(it) }
                ?.let { minecraftServer.getEntityByInstance(it) }
            val silent = ReflectionProxies.commandSourceStack.isSilent(sourceStack)

            return PaperBrigadierSource(source, entity, silent, sourceStack)
        }
    }
}
