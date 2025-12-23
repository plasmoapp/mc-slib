package su.plo.slib.api.server.command.brigadier

import com.mojang.brigadier.arguments.ArgumentType
import org.jetbrains.annotations.ApiStatus
import java.util.ServiceLoader

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

    private val provider: Provider =
        // some loaders can't find service by class's classloader,
        // some can't find it by context class loader
        // so we're just trying both
        ServiceLoader.load(Provider::class.java).firstOrNull()
            ?: ServiceLoader.load(Provider::class.java, Provider::class.java.classLoader).first()

    @ApiStatus.Internal
    interface Provider {
        fun entity(): ArgumentType<Any>

        fun entities(): ArgumentType<Any>

        fun player(): ArgumentType<Any>

        fun players(): ArgumentType<Any>
    }
}
