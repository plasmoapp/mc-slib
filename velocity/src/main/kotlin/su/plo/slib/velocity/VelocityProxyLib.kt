package su.plo.slib.velocity

import com.google.common.collect.Maps
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import su.plo.slib.api.event.player.McPlayerJoinEvent
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.permission.PermissionManager
import su.plo.slib.api.proxy.McProxyLib
import su.plo.slib.api.proxy.event.command.McProxyCommandsRegisterEvent
import su.plo.slib.api.proxy.event.player.McProxyServerConnectedEvent
import su.plo.slib.api.proxy.player.McProxyPlayer
import su.plo.slib.api.proxy.server.McProxyServerInfo
import su.plo.slib.chat.AdventureComponentTextConverter
import su.plo.slib.language.ServerTranslatorFactory
import su.plo.slib.logging.Slf4jLogger
import su.plo.slib.velocity.channel.VelocityChannelManager
import su.plo.slib.velocity.command.VelocityCommandManager
import su.plo.slib.velocity.permission.VelocityPermissionSupplier
import su.plo.slib.velocity.player.VelocityProxyPlayer
import su.plo.slib.velocity.server.VelocityProxyServerInfo
import java.io.File
import java.util.*

class VelocityProxyLib(
    private val proxyServer: ProxyServer,
    plugin: Any
) : McProxyLib {

    private val playerById: MutableMap<UUID, McProxyPlayer> = Maps.newConcurrentMap()
    private val serverByName: MutableMap<String, VelocityProxyServerInfo> = Maps.newConcurrentMap()

    private val permissionSupplier = VelocityPermissionSupplier(this)

    override val serverTranslator = ServerTranslatorFactory.createTranslator()
    override val textConverter = AdventureComponentTextConverter(serverTranslator)

    override val commandManager = VelocityCommandManager(this)
    override val permissionManager = PermissionManager()
    override val channelManager = VelocityChannelManager(proxyServer, this)

    override val servers: Collection<McProxyServerInfo>
        get() = serverByName.values

    override val players: Collection<McProxyPlayer>
        get() = playerById.values

    override val port: Int
        get() = proxyServer.boundAddress.port

    override val configsFolder = File("plugins")

    init {
        McLoggerFactory.supplier = McLoggerFactory.Supplier { name -> Slf4jLogger(name) }

        loadServers()

        // register commands
        McProxyCommandsRegisterEvent.invoker.onCommandsRegister(commandManager, this)
        commandManager.registerCommands(proxyServer)

        proxyServer.eventManager.register(plugin, channelManager)
        proxyServer.eventManager.register(plugin, commandManager)
        proxyServer.eventManager.register(plugin, this)
    }

    override fun getPlayerById(playerId: UUID): McProxyPlayer? =
        playerById[playerId] ?: proxyServer.getPlayer(playerId).map { getPlayerByInstance(it) }.orElse(null)

    override fun getPlayerByName(name: String): McProxyPlayer? =
        proxyServer.getPlayer(name).map { getPlayerByInstance(it) }.orElse(null)

    override fun getPlayerByInstance(instance: Any): McProxyPlayer {
        require(instance is Player) { "instance is not " + Player::class.java }

        return playerById.getOrPut(instance.uniqueId) {
            VelocityProxyPlayer(
                this,
                permissionSupplier,
                instance
            )
        }
    }

    override fun getServerInfoByName(name: String): McProxyServerInfo? {
        serverByName[name]?.let { serverInfo ->
            val server = proxyServer.getServer(name)
            if (!server.isPresent) {
                serverByName.remove(name)
                return null
            }

            if (serverInfo.instance != server.get().serverInfo) {
                return VelocityProxyServerInfo(server.get()).also {
                    serverByName[name] = it
                }
            }

            return serverInfo
        }

        return proxyServer.getServer(name)
            .map { getServerInfoByServerInstance(it) }
            .orElse(null)
    }

    override fun getServerInfoByServerInstance(instance: Any): McProxyServerInfo {
        require(instance is RegisteredServer) { "instance is not " + RegisteredServer::class.java }

        var serverInfo = serverByName[instance.serverInfo.name]
        if (serverInfo == null) {
            serverInfo = VelocityProxyServerInfo(instance)
            serverByName[instance.serverInfo.name] = serverInfo
        } else if (serverInfo.instance != instance.serverInfo) {
            serverInfo = VelocityProxyServerInfo(instance)
            serverByName[instance.serverInfo.name] = serverInfo
        }

        return serverInfo
    }

    @Subscribe
    fun onPlayerJoin(event: PostLoginEvent) {
        McPlayerJoinEvent.invoker.onPlayerJoin(getPlayerByInstance(event.player))
    }

    @Subscribe
    fun onPlayerQuit(event: DisconnectEvent) {
        if (event.loginStatus == DisconnectEvent.LoginStatus.CONFLICTING_LOGIN) return

        McPlayerQuitEvent.invoker.onPlayerQuit(getPlayerByInstance(event.player))
        playerById.remove(event.player.uniqueId)
    }

    @Subscribe
    fun onServerPostConnect(event: ServerPostConnectEvent) {
        val player = getPlayerByInstance(event.player)
        val previousServer = event.previousServer?.let { getServerInfoByServerInstance(it) }

        McProxyServerConnectedEvent.invoker.onServerConnected(player, previousServer)
    }

    private fun loadServers() {
        proxyServer.allServers.forEach {
            getServerInfoByServerInstance(it)
        }
    }
}
