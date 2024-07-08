package su.plo.slib.bungee

import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.event.ServerSwitchEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import su.plo.slib.api.event.player.McPlayerJoinEvent
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.permission.PermissionManager
import su.plo.slib.api.proxy.McProxyLib
import su.plo.slib.api.proxy.event.command.McProxyCommandsRegisterEvent
import su.plo.slib.api.proxy.event.player.McProxyServerConnectedEvent
import su.plo.slib.api.proxy.player.McProxyPlayer
import su.plo.slib.api.proxy.server.McProxyServerInfo
import su.plo.slib.bungee.channel.BungeeChannelManager
import su.plo.slib.bungee.command.BungeeCommandManager
import su.plo.slib.bungee.permission.BungeePermissionSupplier
import su.plo.slib.bungee.player.BungeeProxyPlayer
import su.plo.slib.bungee.server.BungeeProxyServerInfo
import su.plo.slib.chat.AdventureComponentTextConverter
import su.plo.slib.language.ServerTranslatorFactory
import su.plo.slib.logging.JavaLogger
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class BungeeProxyLib(
    private val loader: Plugin
) : McProxyLib, Listener {

    init {
        McLoggerFactory.supplier = McLoggerFactory.Supplier {name ->
            JavaLogger(name).apply { parent = loader.logger.parent }
        }
    }

    private val proxyServer = ProxyServer.getInstance()

    private val playerById: MutableMap<UUID, BungeeProxyPlayer> = ConcurrentHashMap()
    private val serverByName: MutableMap<String, BungeeProxyServerInfo> = ConcurrentHashMap()

    private val permissionSupplier = BungeePermissionSupplier(this)

    override val serverTranslator = ServerTranslatorFactory.createTranslator()
    override val textConverter = AdventureComponentTextConverter()

    override val commandManager = BungeeCommandManager(this)
    override val permissionManager = PermissionManager()
    override val channelManager = BungeeChannelManager(proxyServer, this)

    override val servers: Collection<McProxyServerInfo>
        get() = serverByName.values

    override val players: Collection<McProxyPlayer>
        get() = playerById.values

    override val port: Int
        get() = proxyServer.config.listeners.first().host.port

    override val configsFolder = File("plugins")

    val adventure: BungeeAudiences = BungeeAudiences.create(loader)

    init {
        loadServers()

        // register commands
        McProxyCommandsRegisterEvent.invoker.onCommandsRegister(commandManager, this)
        commandManager.registerCommands(loader, proxyServer)

        proxyServer.pluginManager.registerListener(loader, channelManager)
        proxyServer.pluginManager.registerListener(loader, commandManager)
        proxyServer.pluginManager.registerListener(loader, this)
    }

    override fun getPlayerById(playerId: UUID): McProxyPlayer? =
        proxyServer.getPlayer(playerId)
            ?.let { getPlayerByInstance(it) }

    override fun getPlayerByName(name: String): McProxyPlayer? =
        proxyServer.getPlayer(name)
            ?.let { getPlayerByInstance(it) }

    override fun getPlayerByInstance(instance: Any): McProxyPlayer {
        require(instance is ProxiedPlayer) { "instance is not ${ProxiedPlayer::class.java}" }

        return playerById.getOrPut(instance.uniqueId) {
            BungeeProxyPlayer(this, permissionSupplier, instance)
        }
    }

    override fun getServerInfoByName(name: String): McProxyServerInfo? {
        serverByName[name]?.let { serverInfo ->
            val server = proxyServer.getServerInfo(name) ?: run {
                serverByName.remove(name)
                return null
            }

            if (server != serverInfo.instance) {
                return BungeeProxyServerInfo(server).also {
                    serverByName[name] = it
                }
            }

            return serverInfo
        }

        return proxyServer.getServerInfo(name)?.let { getServerInfoByServerInstance(it) }
    }

    override fun getServerInfoByServerInstance(instance: Any): McProxyServerInfo {
        require(instance is ServerInfo) { "instance is not ${ServerInfo::class.java}" }

        serverByName[instance.name]?.let {
            if (it.instance == instance) return it
        }

        return BungeeProxyServerInfo(instance).also {
            serverByName[instance.name] = it
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PostLoginEvent) {
        McPlayerJoinEvent.invoker.onPlayerJoin(getPlayerByInstance(event.player))
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerDisconnectEvent) {
        McPlayerQuitEvent.invoker.onPlayerQuit(getPlayerByInstance(event.player))
        playerById.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onServerSwitch(event: ServerSwitchEvent) {
        val player = getPlayerByInstance(event.player)
        val previousServer = event.from?.let { getServerInfoByServerInstance(it) }

        McProxyServerConnectedEvent.invoker.onServerConnected(player, previousServer)
    }

    private fun loadServers() {
        proxyServer.servers.values.forEach { serverInfo ->
            serverByName[serverInfo.name] = BungeeProxyServerInfo(serverInfo)
        }
    }
}
