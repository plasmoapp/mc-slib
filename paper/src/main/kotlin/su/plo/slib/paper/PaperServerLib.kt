package su.plo.slib.paper

import com.google.common.collect.ImmutableList
import com.google.common.collect.Maps
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.bukkit.plugin.java.JavaPlugin
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.slib.api.event.player.McPlayerJoinEvent
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.slib.api.event.player.McPlayerVisibilityCheckEvent
import su.plo.slib.api.logging.McLogger
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.permission.PermissionManager
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.api.server.scheduler.McServerScheduler
import su.plo.slib.api.server.world.McServerWorld
import su.plo.slib.chat.AdventureComponentTextConverter
import su.plo.slib.integration.IntegrationLoader
import su.plo.slib.language.ServerTranslatorFactory
import su.plo.slib.paper.channel.PaperChannelManager
import su.plo.slib.paper.channel.RegisterChannelHandler
import su.plo.slib.paper.command.PaperCommandManager
import su.plo.slib.paper.entity.PaperServerEntity
import su.plo.slib.paper.entity.PaperServerPlayer
import su.plo.slib.paper.extension.addChannel
import su.plo.slib.paper.integration.PaperVanishIntegration
import su.plo.slib.paper.integration.PremiumVanishIntegration
import su.plo.slib.paper.permission.PaperPermissionSupplier
import su.plo.slib.paper.scheduler.PaperServerScheduler
import su.plo.slib.paper.util.SchedulerUtil
import su.plo.slib.paper.world.PaperServerWorld
import java.io.File
import java.util.Optional
import java.util.UUID
import java.util.function.Function

class PaperServerLib @JvmOverloads constructor(
    private val loader: JavaPlugin,
    override val baseLogger: McLogger = McLoggerFactory.createLogger("slib"),
) : McServerLib, Listener {

    init {
        instance = this
    }

    private val worldByInstance: MutableMap<World, McServerWorld> = Maps.newConcurrentMap()
    private val playerById: MutableMap<UUID, McServerPlayer> = Maps.newConcurrentMap()

    private val worldFactory = Function<World, McServerWorld> { PaperServerWorld(loader, it) }

    private val permissionSupplier = PaperPermissionSupplier(this)

    override val serverTranslator = ServerTranslatorFactory.createTranslator()
        .also { IntegrationLoader.loadAdventureTranslator(it) }
    override val textConverter = AdventureComponentTextConverter()

    override val commandManager = PaperCommandManager(this)
    override val permissionManager = PermissionManager()
    override val channelManager = PaperChannelManager(loader, this)

    override val scheduler: McServerScheduler = PaperServerScheduler(loader)

    override val worlds
        get() = Bukkit.getWorlds().map(::getWorld)

    override val players
        get() = playerById.values

    override val port
        get() = loader.server.port

    override val version: String
        get() = Bukkit.getVersion().substringAfter("MC: ").substringBefore(")")

    override val configsFolder: File = loader.dataFolder.parentFile

    lateinit var adventure: BukkitAudiences

    fun onInitialize() {
        adventure = BukkitAudiences.create(loader)

        commandManager.registerCommands(loader)
        loader.server.pluginManager.registerEvents(RegisterChannelHandler(this), loader)
        loader.server.pluginManager.registerEvents(this, loader)

        loadVanishIntegrations()
    }

    private fun loadVanishIntegrations() {
        McPlayerVisibilityCheckEvent.registerListener { viewer, target ->
            val viewerPlayer = viewer.getInstance<Player>()
            val targetPlayer = target.getInstance<Player>()

            !viewerPlayer.canSee(targetPlayer)
        }

        val visibilityEventsSupported = try {
            Class.forName("org.bukkit.event.player.PlayerHideEntityEvent")
            Class.forName("org.bukkit.event.player.PlayerShowEntityEvent")
            true
        } catch (_: ClassNotFoundException) {
            false
        }

        val hasPremiumVanish = loader.server.pluginManager.getPlugin("SuperVanish") != null ||
            loader.server.pluginManager.getPlugin("PremiumVanish") != null

        if (hasPremiumVanish) {
            loader.server.pluginManager.registerEvents(PremiumVanishIntegration(this), loader)
            baseLogger.info("PremiumVanish/SuperVanish event listener attached")
        } else if (visibilityEventsSupported) {
            loader.server.pluginManager.registerEvents(PaperVanishIntegration(this, loader), loader)
            baseLogger.info("Paper vanish integration attached")
        }
    }

    fun onShutdown() {
        commandManager.clear(loader)
        permissionManager.clear()
        adventure.close()
    }

    override fun executeInMainThread(runnable: Runnable) {
        SchedulerUtil.runTask(loader, runnable)
    }

    override fun getWorld(instance: Any): McServerWorld {
        require(instance is World) { "instance is not ${World::class.java}" }

        return worldByInstance.computeIfAbsent(instance, worldFactory)
    }

    override fun getPlayerByInstance(instance: Any): McServerPlayer {
        require(instance is Player) { "instance is not ${Player::class.java}" }

        return playerById[instance.uniqueId] ?: instance.wrap()
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

    @Suppress("DEPRECATION")
    override fun getGameProfile(name: String): McGameProfile? =
        Optional.of(Bukkit.getServer().getOfflinePlayer(name))
            .filter { it.isOnline || it.hasPlayedBefore() }
            .map(::getGameProfile)
            .orElse(null)

    private fun getGameProfile(offlinePlayer: OfflinePlayer): McGameProfile =
        // todo: use game profile properties?
        McGameProfile(offlinePlayer.uniqueId, offlinePlayer.name ?: "", ImmutableList.of())

    override fun getEntityByInstance(instance: Any): McServerEntity {
        require(instance is Entity) { "instance is not ${Entity::class.java}" }

        if (instance is Player) {
            return getPlayerByInstance(instance)
        }

        return PaperServerEntity(
            this,
            instance
        )
    }

    @EventHandler(ignoreCancelled = true)
    fun onWorldUnload(event: WorldUnloadEvent) {
        worldByInstance.remove(event.world)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        McPlayerJoinEvent.invoker.onPlayerJoin(getPlayerByInstance(player))

        channelManager.registeredChannels.forEach(player::addChannel)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        McPlayerQuitEvent.invoker.onPlayerQuit(getPlayerByInstance(event.player))
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerJoinEarly(event: PlayerJoinEvent) {
        val player = event.player
        val mcPlayer = player.wrap()
        playerById[player.uniqueId] = mcPlayer
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuitLate(event: PlayerQuitEvent) {
        playerById.remove(event.player.uniqueId)
    }

    private fun Player.wrap(): McServerPlayer =
        PaperServerPlayer(
            loader,
            this@PaperServerLib,
            permissionSupplier,
            this
        )

    companion object {
        lateinit var instance: PaperServerLib
    }
}
