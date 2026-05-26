package su.plo.slib.api.proxy.player

import su.plo.slib.api.proxy.connection.McProxyConnection
import su.plo.slib.api.proxy.connection.McProxyServerConnection
import su.plo.slib.api.entity.player.McPlayer

/**
 * Represents a Minecraft proxy player.
 */
interface McProxyPlayer : McPlayer, McProxyConnection {

    /**
     * Gets the current server for the player.
     */
    val server: McProxyServerConnection?

    /**
     * Determines if this player can see another [player].
     *
     * @param player The player to check visibility for.
     * @return true if this player can see the specified player; otherwise, false.
     */
    fun canSee(player: McProxyPlayer): Boolean

    /**
     * Determines if this player can see another [player].
     *
     * @param player The player to check visibility for.
     * @return true if this player can see the specified player; otherwise, false.
     */
    override fun canSee(player: McPlayer): Boolean = canSee(player as McProxyPlayer)

    /**
     * Gets the server's implementation instance for this player.
     *
     * The return type may vary depending on the server platform:
     *   - For Velocity: todo
     *   - For BungeeCord: todo
     *
     * @return The server's implementation object associated with this entity.
     * @param T The expected type of the entity object.
     */
    override fun <T> getInstance(): T
}
