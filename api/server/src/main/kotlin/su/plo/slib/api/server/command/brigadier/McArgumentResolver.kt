package su.plo.slib.api.server.command.brigadier

import com.mojang.brigadier.context.CommandContext
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.api.service.lazyService

object McArgumentResolver {

    /**
     * Extracts a single entity from a command context and converts it to [McServerEntity].
     *
     * @param context The Brigadier command context.
     * @param name The name of the argument.
     * @return The selected entity.
     */
    @JvmStatic
    fun <S> getEntity(context: CommandContext<S>, name: String): McServerEntity =
        provider.getEntity(context, name)

    /**
     * Extracts multiple entities from a command context and converts them to [McServerEntity].
     *
     * @param context The Brigadier command context.
     * @param name The name of the argument.
     * @return A collection of selected entities.
     */
    @JvmStatic
    fun <S> getEntities(context: CommandContext<S>, name: String): Collection<McServerEntity> =
        provider.getEntities(context, name)

    /**
     * Extracts a single player from a command context and converts it to [McServerPlayer].
     *
     * @param context The Brigadier command context.
     * @param name The name of the argument.
     * @return The selected player.
     */
    @JvmStatic
    fun <S> getPlayer(context: CommandContext<S>, name: String): McServerPlayer =
        provider.getPlayer(context, name)

    /**
     * Extracts multiple players from a command context and converts them to [McServerPlayer].
     *
     * @param context The Brigadier command context.
     * @param name The name of the argument.
     * @return A collection of selected players.
     */
    @JvmStatic
    fun <S> getPlayers(context: CommandContext<S>, name: String): Collection<McServerPlayer> =
        provider.getPlayers(context, name)

    private val provider: Provider by lazyService()
    interface Provider {

        fun <S> getEntity(context: CommandContext<S>, name: String): McServerEntity

        fun <S> getEntities(context: CommandContext<S>, name: String): Collection<McServerEntity>

        fun <S> getPlayer(context: CommandContext<S>, name: String): McServerPlayer

        fun <S> getPlayers(context: CommandContext<S>, name: String): Collection<McServerPlayer>
    }
}
