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
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.api.permission.PermissionsManager
import su.plo.slib.api.server.world.McServerWorld
import su.plo.slib.language.CrowdinServerLanguages
import su.plo.slib.mod.channel.ModChannelManager
import su.plo.slib.mod.chat.ServerComponentTextConverter
import su.plo.slib.mod.command.ModCommandManager
import su.plo.slib.mod.entity.ModServerEntity
import su.plo.slib.mod.entity.ModServerPlayer
import su.plo.slib.mod.permission.ModPermissionSupplier
import su.plo.slib.mod.world.ModServerWorld
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ModServerLib(
    private val minecraftServer: MinecraftServer,
    defaultLanguage: String,
    crowdinDisabled: Boolean
) : McServerLib {

    private val worldByInstance: MutableMap<ServerLevel, McServerWorld> = Maps.newConcurrentMap()
    private val playerById: MutableMap<UUID, McServerPlayer> = Maps.newConcurrentMap()

    private val backgroundExecutor = Executors.newSingleThreadScheduledExecutor()
    private val worldCleanupTask: ScheduledFuture<*>

    private val permissionSupplier = ModPermissionSupplier(this, minecraftServer)

    override val languages = CrowdinServerLanguages(defaultLanguage, crowdinDisabled)
    override val textConverter = ServerComponentTextConverter(languages)

    override val commandManager = ModCommandManager(this)
    override val permissionsManager = PermissionsManager()
    override val channelManager = ModChannelManager()

    override val worlds
        get() = minecraftServer.allLevels.map { getWorld(it) }

    override val players
        get() = playerById.values

    override val port: Int
        get() = minecraftServer.port

    override val version: String
        get() = minecraftServer.serverVersion

    init {
        su.plo.slib.mod.ModServerLib.Companion.INSTANCE = this
        worldCleanupTask = backgroundExecutor.scheduleAtFixedRate(
            { worldsCleanupTick() },
            0L,
            30L,
            TimeUnit.SECONDS
        )

//        PlayerQuitEvent.INSTANCE.registerListener(player -> playerById.remove(player.getUUID()));
    }

    fun onShutdown() {
        commandManager.clear()
        permissionsManager.clear()
        worldCleanupTask.cancel(false)
        backgroundExecutor.shutdown()
    }

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

    override fun getEntity(instance: Any): McServerEntity {
        require(instance is Entity) { "instance is not " + Entity::class.java }

        return ModServerEntity(
            this,
            instance
        )
    }

    private fun worldsCleanupTick() {
        val worlds = minecraftServer.allLevels.toSet()

        worldByInstance.keys
            .filter { !worlds.contains(it) }
            .forEach { worldByInstance.remove(it) }
    }

    companion object {
        var INSTANCE: su.plo.slib.mod.ModServerLib? = null
    }
}
