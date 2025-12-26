package su.plo.slib.bungee.command.brigadier

import net.md_5.bungee.api.CommandSender
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.entity.McEntity

data class BungeeBrigadierSource(
    override val source: McCommandSource,
    override val executor: McEntity? = null,
    private val instance: CommandSender,
) : McBrigadierSource {
    @Suppress("UNCHECKED_CAST")
    override fun <T> getInstance(): T =
        instance as T
}
