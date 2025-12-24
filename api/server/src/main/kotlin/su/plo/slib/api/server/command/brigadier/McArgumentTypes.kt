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
    fun entity(): ArgumentType<Any> = provider.entity()

    /**
     * Returns an argument type that selects multiple entities.
     */
    @JvmStatic
    fun entities(): ArgumentType<Any> = provider.entities()

    /**
     * Returns an argument type that selects a single player.
     */
    @JvmStatic
    fun player(): ArgumentType<Any> = provider.player()

    /**
     * Returns an argument type that selects multiple players.
     */
    @JvmStatic
    fun players(): ArgumentType<Any> = provider.players()

    private val provider: Provider by lazyService()

    @ApiStatus.Internal
    interface Provider {
        fun entity(): ArgumentType<Any>

        fun entities(): ArgumentType<Any>

        fun player(): ArgumentType<Any>

        fun players(): ArgumentType<Any>
    }
}
