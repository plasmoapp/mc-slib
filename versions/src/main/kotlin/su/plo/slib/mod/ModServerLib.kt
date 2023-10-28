package su.plo.slib.mod

import com.google.common.collect.Maps
import com.mojang.authlib.GameProfile
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
import su.plo.slib.language.CrowdinServerLanguages
import su.plo.slib.mod.channel.ModChannelManager
import su.plo.slib.mod.chat.ServerComponentTextConverter
import su.plo.slib.mod.command.ModCommandManager
import su.plo.slib.mod.entity.ModServerEntity
import su.plo.slib.mod.entity.ModServerPlayer
import su.plo.slib.mod.event.server.ServerStoppingEvent
import su.plo.slib.mod.permission.ModPermissionSupplier
import su.plo.slib.mod.world.ModServerWorld
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object ModServerLib : McServerLib {

    lateinit var minecraftServer: MinecraftServer

    private val worldByInstance: MutableMap<ServerLevel, McServerWorld> = Maps.newConcurrentMap()
    private val playerById: MutableMap<UUID, McServerPlayer> = Maps.newConcurrentMap()

    private val backgroundExecutor = Executors.newSingleThreadScheduledExecutor()
    private lateinit var worldCleanupTask: ScheduledFuture<*>

    private val permissionSupplier = ModPermissionSupplier(this)

    override val languages = CrowdinServerLanguages()
    override val textConverter = ServerComponentTextConverter(languages)

    override val commandManager = ModCommandManager(this)
    override val permissionManager = PermissionManager()
    override val channelManager = ModChannelManager()

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

        return worldByInstance.computeIfAbsent(instance) {
            ModServerWorld(
                instance.dimension().location(),
                instance
            )
        }
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

    override fun getEntity(instance: Any): McServerEntity {
        require(instance is Entity) { "instance is not " + Entity::class.java }

        return ModServerEntity(
            this,
            instance
        )
    }

    override fun getGameProfile(playerId: UUID): McGameProfile? {
        //#if MC>=11701
        return minecraftServer.profileCache.get(playerId).orElse(null)?.let { convertGameProfile(it) }
        //#else
        //$$ return minecraftServer.profileCache.get(playerId)?.let { convertGameProfile(it) }
        //#endif
    }

    override fun getGameProfile(name: String): McGameProfile? {
        //#if MC>=11701
        return minecraftServer.profileCache.get(name).orElse(null)?.let { convertGameProfile(it) }
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
        this.worldCleanupTask = backgroundExecutor.scheduleAtFixedRate(
            { worldsCleanupTick() },
            0L,
            30L,
            TimeUnit.SECONDS
        )

        McPlayerQuitEvent.registerListener(::onPlayerQuit)
        ServerStoppingEvent.registerListener(::onServerStopping)
    }

    private fun onShutdown() {
        commandManager.clear()
        permissionManager.clear()
        worldCleanupTask.cancel(false)
        backgroundExecutor.shutdown()

        McPlayerQuitEvent.unregisterListener(::onPlayerQuit)
        ServerStoppingEvent.unregisterListener(::onServerStopping)
    }
}
