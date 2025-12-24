package su.plo.slib.minestom.command.brigadier

import net.minestom.server.command.CommandSender
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.entity.McEntity

data class MinestomBrigadierSource(
    val sender: CommandSender,
    override val source: McCommandSource,
    override val executor: McEntity?
) : McBrigadierSource

class MinestomBrigadierSourceProvider : McBrigadierSource.Provider {
    override fun <S> getBrigadierSource(source: S): McBrigadierSource =
        source as MinestomBrigadierSource
}
