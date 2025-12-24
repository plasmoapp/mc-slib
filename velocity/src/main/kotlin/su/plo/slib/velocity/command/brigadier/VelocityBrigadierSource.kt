package su.plo.slib.velocity.command.brigadier

import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.CommandSource
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.entity.McEntity
import su.plo.slib.velocity.VelocityProxyLib

data class VelocityBrigadierSource(
    override val source: McCommandSource,
    override val executor: McEntity? = null,
) : McBrigadierSource

class VelocityBrigadierSourceProvider : McBrigadierSource.Provider {
    private val minecraftProxy by lazy { VelocityProxyLib.instance }

    override fun <S> getBrigadierSource(context: CommandContext<S>): McBrigadierSource {
        val source = context.source as CommandSource

        return VelocityBrigadierSource(minecraftProxy.commandManager.getCommandSource(source))
    }
}
