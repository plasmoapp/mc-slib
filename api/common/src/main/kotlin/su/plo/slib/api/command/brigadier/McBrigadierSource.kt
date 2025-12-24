package su.plo.slib.api.command.brigadier

import com.mojang.brigadier.context.CommandContext
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.entity.McEntity
import su.plo.slib.api.service.lazyService

interface McBrigadierSource {
    /**
     * Gets the command source that initiated/triggered the execution of a command.
     */
    val source: McCommandSource

    /**
     * Gets the entity executing this command.
     */
    val executor: McEntity?

    companion object {
        private val provider: Provider by lazyService()

        /**
         * Gets a brigadier source by server-specific instance.
         *
         * @param context The server-specific command context instance.
         * @return A [McBrigadierSource] instance corresponding to the provided command source instance.
         */
        @JvmStatic
        fun <S> from(context: CommandContext<S>): McBrigadierSource =
            provider.getBrigadierSource(context)
    }

    interface Provider {
        fun <S> getBrigadierSource(context: CommandContext<S>): McBrigadierSource
    }
}
