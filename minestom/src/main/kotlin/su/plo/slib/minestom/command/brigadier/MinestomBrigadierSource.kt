package su.plo.slib.minestom.command.brigadier

import com.mojang.brigadier.context.CommandContext
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
    override fun <S> getBrigadierSource(context: CommandContext<S>): McBrigadierSource =
        context.source as MinestomBrigadierSource
}
