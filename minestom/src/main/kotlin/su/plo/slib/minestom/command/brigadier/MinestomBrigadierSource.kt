package su.plo.slib.minestom.command.brigadier

import net.minestom.server.command.CommandSender
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.entity.McEntity

data class MinestomBrigadierSource(
    override val source: McCommandSource,
    override val executor: McEntity?,
    private val instance: CommandSender,
) : McBrigadierSource {
    @Suppress("UNCHECKED_CAST")
    override fun <T> getInstance(): T =
        instance as T
}
