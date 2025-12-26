package su.plo.slib.spigot.command.brigadier

import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.entity.McEntity
import su.plo.slib.spigot.SpigotServerLib
import su.plo.slib.spigot.nms.ReflectionProxies

data class SpigotBrigadierSource(
    override val source: McCommandSource,
    override val executor: McEntity?,
    private val instance: Any,
): McBrigadierSource {

    @Suppress("UNCHECKED_CAST")
    override fun <T> getInstance(): T =
        instance as T

    companion object {
        private val minecraftServer by lazy { SpigotServerLib.instance }

        fun from(sourceStack: Any): SpigotBrigadierSource {
            val source = ReflectionProxies.commandSourceStack.getBukkitSender(sourceStack)
                .let { minecraftServer.commandManager.getCommandSource(it) }
            val entity = ReflectionProxies.commandSourceStack.getEntity(sourceStack)
                ?.let { ReflectionProxies.entity.getBukkitEntity(it) }
                ?.let { minecraftServer.getEntityByInstance(it) }

            return SpigotBrigadierSource(source, entity, sourceStack)
        }
    }
}
