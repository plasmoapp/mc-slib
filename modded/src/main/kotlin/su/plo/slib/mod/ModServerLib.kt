package su.plo.slib.mod

import com.google.common.collect.Maps
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
import su.plo.slib.mod.extension.toMcGameProfile
import su.plo.slib.mod.logging.Log4jLogger
import su.plo.slib.mod.permission.ModPermissionSupplier
import su.plo.slib.mod.scheduler.ModServerScheduler
import su.plo.slib.mod.world.ModServerWorld
import java.io.File
import java.lang.Runnable
import java.util.*
import kotlin.time.Duration.Companion.seconds

//? if >=1.21.9 {
/*import com.mojang.authlib.GameProfile
*///?}

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

        if (instance is ServerPlayer) {
            return getPlayerByInstance(instance)
        }

        return ModServerEntity(
            this,
            instance
        )
    }

    override fun getGameProfile(playerId: UUID): McGameProfile? {
        //? if >=1.21.9 {
        /*return minecraftServer.services().nameToIdCache.get(playerId)
            .map {
                minecraftServer.services().profileResolver.fetchById(playerId)
                    .orElse(GameProfile(it.id, it.name))
            }
            .map { it.toMcGameProfile() }
            .orElse(null)
        *///?} elif >=1.17.1 {
        return minecraftServer.profileCache?.get(playerId)?.orElse(null)?.toMcGameProfile()
        //?} else {
        /*return minecraftServer.profileCache.get(playerId)?.toMcGameProfile()
        *///?}
    }

    override fun getGameProfile(name: String): McGameProfile? {
        //? if >=1.21.9 {
        /*return minecraftServer.services().nameToIdCache.get(name)
            .map {
                minecraftServer.services().profileResolver.fetchByName(name)
                    .orElse(GameProfile(it.id, it.name))
            }
            .map { it.toMcGameProfile() }
            .orElse(null)
        *///?} elif >=1.17.1 {
        return minecraftServer.profileCache?.get(name)?.orElse(null)?.toMcGameProfile()
        //?} else {
        /*return minecraftServer.profileCache.get(name)?.toMcGameProfile()
        *///?}
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
