package su.plo.slib.mod

import com.google.common.collect.Maps
import com.mojang.authlib.GameProfile
import kotlinx.coroutines.*
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.slib.api.entity.player.McPlayer
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.api.permission.PermissionManager
import su.plo.slib.api.server.world.McServerWorld
import su.plo.slib.language.ServerTranslatorFactory
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.server.scheduler.McServerScheduler
import su.plo.slib.chat.AdventureComponentTextConverter
import su.plo.slib.integration.IntegrationLoader
import su.plo.slib.mod.channel.ModChannelManager
import su.plo.slib.mod.command.ModCommandManager
import su.plo.slib.mod.entity.ModServerEntity
import su.plo.slib.mod.entity.ModServerPlayer
import su.plo.slib.mod.event.server.ServerStoppingEvent
import su.plo.slib.mod.logging.Log4jLogger
import su.plo.slib.mod.permission.ModPermissionSupplier
import su.plo.slib.mod.scheduler.ModServerScheduler
import su.plo.slib.mod.world.ModServerWorld
import java.io.File
import java.lang.Runnable
import java.util.*
import kotlin.time.Duration.Companion.seconds

object ModServerLib : McServerLib {

    init {
        McLoggerFactory.supplier = McLoggerFactory.Supplier { name -> Log4jLogger(name) }
    }

    lateinit var minecraftServer: MinecraftServer

    private val worldByInstance: MutableMap<ServerLevel, McServerWorld> = Maps.newConcurrentMap()
    private val playerById: MutableMap<UUID, McServerPlayer> = Maps.newConcurrentMap()

    private var worldCleanupJob: Job? = null

    private val permissionSupplier = ModPermissionSupplier(this)

    override val serverTranslator = ServerTranslatorFactory.createTranslator()
        .also { IntegrationLoader.loadAdventureTranslator(it) }
    override val textConverter = AdventureComponentTextConverter()

    override val commandManager = ModCommandManager(this)
    override val permissionManager = PermissionManager()
    override val channelManager = ModChannelManager()

    override val scheduler: McServerScheduler = ModServerScheduler()

    override val worlds
        get() = minecraftServer.allLevels.map { getWorld(it) }

    override val players
        get() = playerById.values

    override val port: Int
        get() = minecraftServer.port

    override val version: String
        get() = minecraftServer.serverVersion

    override val configsFolder = File("config")

    override fun executeInMainThread(runnable: Runnable) {
        minecraftServer.execute(runnable)
    }

    override fun getWorld(instance: Any): McServerWorld {
        require(instance is ServerLevel) { "instance is not " + ServerLevel::class.java }

        return worldByInstance.computeIfAbsent(instance) { ModServerWorld(instance) }
    }

    override fun getPlayerByInstance(instance: Any): McServerPlayer {
        require(instance is ServerPlayer) { "instance is not " + ServerPlayer::class.java }

        var serverPlayer = playerById[instance.uuid]
        if (serverPlayer == null) {
            serverPlayer = ModServerPlayer(
                this,
                permissionSupplier,
                instance
            )
            playerById[instance.uuid] = serverPlayer
        } else if (serverPlayer.getInstance<Any>() !== instance) {
            (serverPlayer as ModServerPlayer).instance = instance
        }

        return serverPlayer
    }

    override fun getPlayerByName(name: String): McServerPlayer? =
        minecraftServer.playerList.getPlayerByName(name)?.let { getPlayerByInstance(it) }

    override fun getPlayerById(playerId: UUID): McServerPlayer? =
        playerById[playerId] ?: minecraftServer.playerList.getPlayer(playerId)?.let { getPlayerByInstance(it) }

    override fun getEntityByInstance(instance: Any): McServerEntity {
        require(instance is Entity) { "instance is not " + Entity::class.java }

        return ModServerEntity(
            this,
            instance
        )
    }

    override fun getGameProfile(playerId: UUID): McGameProfile? {
        //#if MC>=11701
        return minecraftServer.profileCache?.get(playerId)?.orElse(null)?.let { convertGameProfile(it) }
        //#else
        //$$ return minecraftServer.profileCache.get(playerId)?.let { convertGameProfile(it) }
        //#endif
    }

    override fun getGameProfile(name: String): McGameProfile? {
        //#if MC>=11701
        return minecraftServer.profileCache?.get(name)?.orElse(null)?.let { convertGameProfile(it) }
        //#else
        //$$ return minecraftServer.profileCache.get(name)?.let { convertGameProfile(it) }
        //#endif
    }

    private fun convertGameProfile(gameProfile: GameProfile): McGameProfile {
        return McGameProfile(
            gameProfile.id,
            gameProfile.name,
            gameProfile.properties.values().map {
                McGameProfile.Property(it.name, it.value, it.signature)
            }
        )
    }

    private fun worldsCleanupTick() {
        val worlds = minecraftServer.allLevels.toSet()

        worldByInstance.keys
            .filter { !worlds.contains(it) }
            .forEach { worldByInstance.remove(it) }
    }

    private fun onPlayerQuit(player: McPlayer) {
        playerById.remove(player.uuid)
    }

    private fun onServerStopping(server: MinecraftServer) {
        onShutdown()
    }

    fun onInitialize(minecraftServer: MinecraftServer) {
        this.minecraftServer = minecraftServer
        this.worldCleanupJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                delay(30.seconds)
                worldsCleanupTick()
            }
        }

        McPlayerQuitEvent.registerListener(::onPlayerQuit)
        ServerStoppingEvent.registerListener(::onServerStopping)
    }

    private fun onShutdown() {
        commandManager.clear()
        permissionManager.clear()
        worldCleanupJob?.cancel()

        McPlayerQuitEvent.unregisterListener(::onPlayerQuit)
        ServerStoppingEvent.unregisterListener(::onServerStopping)
    }
}
