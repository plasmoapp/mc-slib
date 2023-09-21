package su.plo.slib.api.proxy

import su.plo.slib.api.McLib
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.proxy.command.McProxyCommand
import su.plo.slib.api.permission.PermissionsManager
import su.plo.slib.api.proxy.player.McProxyPlayer
import su.plo.slib.api.proxy.server.McProxyServerInfo
import java.util.*

interface McProxyLib : McLib {

    /**
     * @see McCommandManager
     */
    override val commandManager: McCommandManager<McProxyCommand>

    /**
     * @see PermissionsManager
     */
    override val permissionsManager: PermissionsManager

    /**
     * Gets player by unique id
     */
    fun getPlayerById(playerId: UUID): McProxyPlayer?

    /**
     * Gets player by name
     */
    fun getPlayerByName(name: String): McProxyPlayer?

    /**
     * instance [Any] can be:
     *
     *  * `com.velocitypowered.api.proxy.Player` for velocity
     *  * `net.md_5.bungee.api.connection.ProxiedPlayer` for bungee
     *
     * @return [McProxyPlayer] by server's instance
     */
    fun getPlayerByInstance(instance: Any): McProxyPlayer

    /**
     * Gets all players connected to the proxy
     */
    val players: Collection<McProxyPlayer>

    /**
     * Gets server info by name
     */
    fun getServerInfoByName(name: String): McProxyServerInfo?

    /**
     * instance [Any] can be:
     *
     *  * `com.velocitypowered.api.proxy.server.RegisteredServer` for velocity
     *  * `net.md_5.bungee.api.config.ServerInfo` for bungee
     *
     * @return [McProxyServerInfo] by server's instance
     */
    fun getServerInfoByServerInstance(instance: Any): McProxyServerInfo

    /**
     * Gets registered backend servers
     */
    val servers: Collection<McProxyServerInfo>

    /**
     * Gets proxy's port
     */
    val port: Int
}
