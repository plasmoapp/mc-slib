package su.plo.slib.api.proxy

import su.plo.slib.api.McLib
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.proxy.channel.McProxyChannelManager
import su.plo.slib.api.proxy.command.McProxyCommand
import su.plo.slib.api.proxy.player.McProxyPlayer
import su.plo.slib.api.proxy.server.McProxyServerInfo
import java.util.*

/**
 * Represents a Minecraft proxy library.
 */
interface McProxyLib : McLib {

    override val commandManager: McCommandManager<McProxyCommand>

    /***
     * @see McProxyChannelManager
     */
    val channelManager: McProxyChannelManager

    /**
     * Gets a proxy player by their unique identifier.
     *
     * @param playerId The unique identifier of the player.
     * @return A [McProxyPlayer] instance representing the player, or `null` if not found.
     */
    fun getPlayerById(playerId: UUID): McProxyPlayer?

    /**
     * Gets a proxy player by their name.
     *
     * @param name The name of the player.
     * @return A [McProxyPlayer] instance representing the player, or `null` if not found.
     */
    fun getPlayerByName(name: String): McProxyPlayer?


    /**
     * Gets a proxy player by their server-specific instance.
     *
     * The [instance] parameter represents the server-specific player instance:
     *   - For Velocity: `com.velocitypowered.api.proxy.Player`
     *   - For BungeeCord: `net.md_5.bungee.api.connection.ProxiedPlayer`
     *
     * @param instance The server-specific player instance.
     * @return A [McProxyPlayer] instance corresponding to the provided server instance.
     */
    fun getPlayerByInstance(instance: Any): McProxyPlayer

    /**
     * Gets a collection of all players currently connected to the proxy.
     *
     * @return A collection of players.
     */
    val players: Collection<McProxyPlayer>

    /**
     * Gets server information by server name.
     *
     * @param name The name of the backend server.
     * @return A [McProxyServerInfo] instance representing the server information, or `null` if not found.
     */
    fun getServerInfoByName(name: String): McProxyServerInfo?

    /**
     * Gets server information by server-specific instance.
     *
     * The [instance] parameter represents the server-specific server instance:
     *  - For Velocity `com.velocitypowered.api.proxy.server.RegisteredServer`
     *  - For BungeeCord `net.md_5.bungee.api.config.ServerInfo`
     *
     * @param instance The server-specific server instance.
     * @return A [McProxyServerInfo] instance corresponding to the provided server instance.
     */
    fun getServerInfoByServerInstance(instance: Any): McProxyServerInfo

    /**
     * Gets registered backend servers.
     *
     * @return A collection of backend servers.
     */
    val servers: Collection<McProxyServerInfo>

    /**
     * Gets the bound port of the proxy server.
     *
     * @return The bound port.
     */
    val port: Int
}
