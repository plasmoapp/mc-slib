package su.plo.slib.api.server.command.brigadier

import com.mojang.brigadier.arguments.ArgumentType
import org.jetbrains.annotations.ApiStatus
import su.plo.slib.api.service.lazyService

/**
 * Vanilla Minecraft argument types.
 */
object McArgumentTypes {

    /**
     * Returns an argument type that selects a single entity.
     */
    @JvmStatic
    fun entity(): ArgumentType<McEntityArgumentResolver> = provider.entity()

    /**
     * Returns an argument type that selects multiple entities.
     */
    @JvmStatic
    fun entities(): ArgumentType<McEntitiesArgumentResolver> = provider.entities()

    /**
     * Returns an argument type that selects a single player.
     */
    @JvmStatic
    fun player(): ArgumentType<McPlayerArgumentResolver> = provider.player()

    /**
     * Returns an argument type that selects multiple players.
     */
    @JvmStatic
    fun players(): ArgumentType<McPlayersArgumentResolver> = provider.players()

    /**
     * Returns an argument type that resolves position.
     */
    @JvmStatic
    fun position(): ArgumentType<ServerPos3dResolver> = provider.position()

    private val provider: Provider by lazyService()

    @ApiStatus.Internal
    interface Provider {
        fun entity(): ArgumentType<McEntityArgumentResolver>

        fun entities(): ArgumentType<McEntitiesArgumentResolver>

        fun player(): ArgumentType<McPlayerArgumentResolver>

        fun players(): ArgumentType<McPlayersArgumentResolver>

        fun position(): ArgumentType<ServerPos3dResolver>
    }
}
