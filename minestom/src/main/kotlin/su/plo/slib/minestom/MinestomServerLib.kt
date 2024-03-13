package su.plo.slib.minestom

import com.google.common.collect.Maps
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.Player
import net.minestom.server.event.instance.InstanceUnregisterEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerPluginMessageEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.extensions.Extension
import net.minestom.server.instance.Instance
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.slib.api.event.player.McPlayerJoinEvent
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.slib.api.permission.PermissionManager
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.api.server.world.McServerWorld
import su.plo.slib.chat.AdventureComponentTextConverter
import su.plo.slib.language.ServerTranslatorFactory
import su.plo.slib.minestom.channel.RegisterChannelHandler
import su.plo.slib.minestom.channel.MinestomChannelManager
import su.plo.slib.minestom.command.MinestomCommandManager
import su.plo.slib.minestom.entity.MinestomServerEntity
import su.plo.slib.minestom.entity.MinestomServerPlayer
import su.plo.slib.minestom.permission.MinestomPermissionSupplier
import su.plo.slib.minestom.world.MinestomServerWorld
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import java.util.function.Consumer

class MinestomServerLib(
    private val extension: Extension
) : McServerLib {

    private val worldByInstance: MutableMap<Instance, McServerWorld> = Maps.newConcurrentMap()
    private val playerById: MutableMap<UUID, McServerPlayer> = Maps.newConcurrentMap()

    private val permissionSupplier = MinestomPermissionSupplier(this)

    override val serverTranslator = ServerTranslatorFactory.createTranslator()
    override val textConverter = AdventureComponentTextConverter(serverTranslator)

    override val commandManager = MinestomCommandManager(this)
    override val permissionManager = PermissionManager()
    override val channelManager = MinestomChannelManager(extension, this)

    override val worlds
        get() = MinecraftServer.getInstanceManager().instances.map(::getWorld)

    override val players
        get() = playerById.values

    override val port
        get() = MinecraftServer.getServer().port

    override val version: String
        get() = MinecraftServer.VERSION_NAME

    override val configsFolder: File = extension.dataDirectory.toFile()

    fun onInitialize() {
        commandManager.registerCommands()

        extension.eventNode.addListener(PlayerPluginMessageEvent::class.java, RegisterChannelHandler(this).pluginChannelRegisterFaker)
        extension.eventNode.addListener(InstanceUnregisterEvent::class.java, instanceUnloadListener)
        extension.eventNode.addListener(PlayerSpawnEvent::class.java, playerJoinListener)
        extension.eventNode.addListener(PlayerDisconnectEvent::class.java, playerDisconnectListener)
    }

    fun onShutdown() {
        commandManager.clear()
        permissionManager.clear()
    }

    override fun executeInMainThread(runnable: Runnable) {
        MinecraftServer.getSchedulerManager().scheduleNextTick(runnable)
    }

    override fun getWorld(instance: Any): McServerWorld {
        require(instance is Instance) { "instance is not ${Instance::class.java}" }

        return worldByInstance.computeIfAbsent(
            instance
        ) { MinestomServerWorld(instance) }
    }

    override fun getPlayerByInstance(instance: Any): McServerPlayer {
        require(instance is Player) { "instance is not ${Player::class.java}" }

        var serverPlayer = playerById[instance.uuid]
        if ((serverPlayer?.getInstance() as? Player)?.entityId != instance.entityId) {
            serverPlayer = MinestomServerPlayer(
                this,
                permissionSupplier,
                instance
            )

            playerById[instance.uuid] = serverPlayer
        }

        return serverPlayer
    }

    override fun getPlayerByName(name: String): McServerPlayer? =
        MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(name)?.let { getPlayerByInstance(it) }

    override fun getPlayerById(playerId: UUID): McServerPlayer? =
        playerById[playerId] ?: MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerId)?.let { getPlayerByInstance(it) }

    override fun getGameProfile(playerId: UUID): McGameProfile? =
            this.getGameProfile(MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(playerId))

    override fun getGameProfile(name: String): McGameProfile? =
            this.getGameProfile(MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(name))

    private fun getGameProfile(maybePlayer: Player?): McGameProfile? {
        val player = maybePlayer ?: run { return null }
        // todo: use game profile properties?
        return McGameProfile(player.uuid, player.username, listOf())
    }

    override fun getEntityByInstance(instance: Any): McServerEntity {
        require(instance is LivingEntity) { "instance is not ${LivingEntity::class.java}" }

        return MinestomServerEntity(
            this,
            instance
        )
    }

    private val instanceUnloadListener: Consumer<InstanceUnregisterEvent> = Consumer { event ->
        worldByInstance.remove(event.instance)
    }

    private val playerJoinListener: Consumer<PlayerSpawnEvent> = Consumer { event ->
        val player = getPlayerByInstance(event.player) as MinestomServerPlayer
        McPlayerJoinEvent.invoker.onPlayerJoin(player)

        // Minestom does not manage plugin message channels, we have to replicate bukkit behaviour
        val stream = ByteArrayOutputStream()
        for (channel in channelManager.registeredChannels) {
            try {
                stream.write(channel.toByteArray(charset("UTF8")))
                stream.write("\u0000".toByteArray(charset("UTF-8")))
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
        event.player.sendPluginMessage("minecraft:register", stream.toByteArray())

        channelManager.registeredChannels.forEach {
            player.registeredChannels.add(it)
        }
    }

    private val playerDisconnectListener: Consumer<PlayerDisconnectEvent> = Consumer { event ->
        McPlayerQuitEvent.invoker.onPlayerQuit(
                getPlayerByInstance(event.player)
        )
        playerById.remove(event.player.uuid)
    }
}
