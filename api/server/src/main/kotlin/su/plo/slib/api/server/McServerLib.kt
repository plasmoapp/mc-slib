package su.plo.slib.api.server

import su.plo.slib.api.McLib
import su.plo.slib.api.server.channel.McChannelManager
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.api.server.world.McServerWorld
import java.util.*

interface McServerLib : McLib {

    /**
     * @see McCommandManager
     */
    override val commandManager: McCommandManager<McCommand>

    /***
     * @see McChannelManager
     */
    val channelManager: McChannelManager

    /**
     * Executes the task on main thread
     */
    fun executeInMainThread(runnable: Runnable)

    /**
     * instance [Object] can be:
     *
     *  * `org.bukkit.World` for bukkit
     *  * `net.minecraft.server.level.ServerLevel` for mods (fabric/forge)
     *
     * @return [McServerWorld] by server's instance
     */
    fun getWorld(instance: Any): McServerWorld

    /**
     * @return collection of all worlds
     */
    val worlds: Collection<McServerWorld>

    /**
     * instance [Object] can be:
     *
     *  * `org.bukkit.entity.Player` for bukkit
     *  * `net.minecraft.server.level.ServerPlayer` for mods (fabric/forge)
     *
     * @return [McServerPlayer] by server's instance
     */
    fun getPlayerByInstance(instance: Any): McServerPlayer

    /**
     * @return [McServerPlayer] by name if exists
     */
    fun getPlayerByName(name: String): McServerPlayer?

    /**
     * @return [McServerPlayer] by uuid if exists
     */
    fun getPlayerById(playerId: UUID): McServerPlayer?

    /**
     * @return collection of all online players
     */
    val players: Collection<McServerPlayer>

    /**
     * @return [McGameProfile] by player's uuid if exists
     */
    fun getGameProfile(playerId: UUID): McGameProfile?

    /**
     * @return [McGameProfile] by player's name if exists
     */
    fun getGameProfile(name: String): McGameProfile?

    /**
     * instance [Object] can be:
     *
     *  * `org.bukkit.entity.LivingEntity` for bukkit
     *  * `net.minecraft.world.entity.Entity` for mods (fabric/forge mojmap)
     *
     * @return [McServerEntity] by server's instance
     */
    fun getEntity(instance: Any): McServerEntity

    /**
     * @return server's port
     */
    val port: Int

    /**
     * @return minecraft server version
     */
    val version: String
}
