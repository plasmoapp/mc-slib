package su.plo.slib.spigot

import com.google.common.collect.ImmutableList
import com.google.common.collect.Maps
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldUnloadEvent
import org.bukkit.plugin.java.JavaPlugin
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.slib.api.event.player.McPlayerJoinEvent
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.slib.api.permission.PermissionManager
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.api.server.world.McServerWorld
import su.plo.slib.spigot.channel.RegisterChannelHandler
import su.plo.slib.spigot.channel.SpigotChannelManager
import su.plo.slib.spigot.chat.BaseComponentTextConverter
import su.plo.slib.spigot.command.SpigotCommandManager
import su.plo.slib.spigot.entity.SpigotServerEntity
import su.plo.slib.spigot.entity.SpigotServerPlayer
import su.plo.slib.spigot.permission.SpigotPermissionSupplier
import su.plo.slib.spigot.world.SpigotServerWorld
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class SpigotServerLib(
    private val loader: JavaPlugin
) : McServerLib, Listener {

    private val worldByInstance: MutableMap<World, McServerWorld> = Maps.newConcurrentMap()
    private val playerById: MutableMap<UUID, McServerPlayer> = Maps.newConcurrentMap()

    private val permissionSupplier = SpigotPermissionSupplier(this)

    val backgroundExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    override val textConverter = BaseComponentTextConverter()

    override val commandManager = SpigotCommandManager(this)
    override val permissionManager = PermissionManager()
    override val channelManager = SpigotChannelManager(loader, this)

    override val worlds
        get() = Bukkit.getWorlds().map(::getWorld)

    override val players
        get() = playerById.values

    override val port
        get() = loader.server.port

    override val version: String
        get() = Bukkit.getVersion().substringAfter("MC: ").substringBefore(")")

    override val configsFolder = Bukkit.getPluginsFolder()

    fun onInitialize() {
        commandManager.registerCommands(loader)
        loader.server.pluginManager.registerEvents(RegisterChannelHandler(this), loader)
        loader.server.pluginManager.registerEvents(this, loader)
    }

    fun onShutdown() {
        commandManager.clear()
        permissionManager.clear()
        backgroundExecutor.shutdown()
    }

    override fun executeInMainThread(runnable: Runnable) {
        Bukkit.getServer().scheduler.runTask(loader, runnable)
    }

    override fun getWorld(instance: Any): McServerWorld {
        require(instance is World) { "instance is not ${World::class.java}" }

        return worldByInstance.computeIfAbsent(
            instance
        ) { SpigotServerWorld(loader, instance) }
    }

    override fun getPlayerByInstance(instance: Any): McServerPlayer {
        require(instance is Player) { "instance is not ${Player::class.java}" }

        var serverPlayer = playerById[instance.uniqueId]
        if ((serverPlayer?.getInstance() as? Player)?.entityId != instance.entityId) {
            serverPlayer = SpigotServerPlayer(
                loader,
                this,
                permissionSupplier,
                instance
            )

            playerById[instance.uniqueId] = serverPlayer
        }

        return serverPlayer
    }

    override fun getPlayerByName(name: String): McServerPlayer? =
        Bukkit.getPlayer(name)?.let { getPlayerByInstance(it) }

    override fun getPlayerById(playerId: UUID): McServerPlayer? =
        playerById[playerId] ?: Bukkit.getPlayer(playerId)?.let { getPlayerByInstance(it) }

    override fun getGameProfile(playerId: UUID): McGameProfile? =
        Optional.of(Bukkit.getServer().getOfflinePlayer(playerId))
            .filter { it.isOnline || it.hasPlayedBefore() }
            .map(::getGameProfile)
            .orElse(null)

    override fun getGameProfile(name: String): McGameProfile? =
        Optional.of(Bukkit.getServer().getOfflinePlayer(name))
            .filter { it.isOnline || it.hasPlayedBefore() }
            .map(::getGameProfile)
            .orElse(null)

    private fun getGameProfile(offlinePlayer: OfflinePlayer): McGameProfile =
        // todo: use game profile properties?
        McGameProfile(offlinePlayer.uniqueId, offlinePlayer.name ?: "", ImmutableList.of())

    override fun getEntity(instance: Any): McServerEntity {
        require(instance is LivingEntity) { "instance is not ${LivingEntity::class.java}" }

        return SpigotServerEntity(
            this,
            instance
        )
    }

    @EventHandler(ignoreCancelled = true)
    fun onWorldUnload(event: WorldUnloadEvent) {
        worldByInstance.remove(event.world)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerJoin(event: org.bukkit.event.player.PlayerJoinEvent) {
        McPlayerJoinEvent.invoker.onPlayerJoin(
            getPlayerByInstance(event.player)
        )
    }

    @EventHandler
    fun onPlayerQuit(event: org.bukkit.event.player.PlayerQuitEvent) {
        McPlayerQuitEvent.invoker.onPlayerQuit(
            getPlayerByInstance(event.player)
        )
        playerById.remove(event.player.uniqueId)
    }
}
