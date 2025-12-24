package su.plo.slib.bungee.command.brigadier

import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.entity.McEntity

data class BungeeBrigadierSource(
    override val source: McCommandSource,
    override val executor: McEntity? = null,
) : McBrigadierSource

class BungeeBrigadierSourceProvider : McBrigadierSource.Provider {
    override fun <S> getBrigadierSource(source: S): McBrigadierSource =
        source as BungeeBrigadierSource
}
