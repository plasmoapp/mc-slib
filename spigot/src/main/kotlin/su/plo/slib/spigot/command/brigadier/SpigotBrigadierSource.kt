package su.plo.slib.spigot.command.brigadier

import com.mojang.brigadier.context.CommandContext
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.entity.McEntity
import su.plo.slib.spigot.SpigotServerLib
import su.plo.slib.spigot.nms.ReflectionProxies

data class SpigotBrigadierSource(
    override val source: McCommandSource,
    override val executor: McEntity?,
): McBrigadierSource

class SpigotBrigadierSourceProvider : McBrigadierSource.Provider {
    private val minecraftServer by lazy { SpigotServerLib.instance }

    override fun <S> getBrigadierSource(context: CommandContext<S>): McBrigadierSource {
        val sourceStack = context.source as Any

        val source = ReflectionProxies.commandSourceStack.getBukkitSender(sourceStack)
            .let { minecraftServer.commandManager.getCommandSource(it) }
        val entity = ReflectionProxies.commandSourceStack.getEntity(sourceStack)
            ?.let { ReflectionProxies.entity.getBukkitEntity(it) }
            ?.let { minecraftServer.getEntityByInstance(it) }

        return SpigotBrigadierSource(source, entity)
    }
}
