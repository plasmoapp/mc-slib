package su.plo.slib.velocity.command.brigadier

import com.velocitypowered.api.command.CommandSource
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.entity.McEntity
import su.plo.slib.velocity.VelocityProxyLib

data class VelocityBrigadierSource(
    override val source: McCommandSource,
    override val executor: McEntity? = null,
    private val instance: CommandSource,
) : McBrigadierSource {
    @Suppress("UNCHECKED_CAST")
    override fun <T> getInstance(): T =
        instance as T

    companion object {
        private val minecraftProxy by lazy { VelocityProxyLib.instance }

        fun from(source: CommandSource): VelocityBrigadierSource =
            VelocityBrigadierSource(minecraftProxy.commandManager.getCommandSource(source), instance = source)
    }
}
