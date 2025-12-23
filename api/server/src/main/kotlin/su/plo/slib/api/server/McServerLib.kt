package su.plo.slib.api.server

import su.plo.slib.api.McLib
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.slib.api.server.channel.McServerChannelManager
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.api.server.scheduler.McServerScheduler
import su.plo.slib.api.server.world.McServerWorld
import java.util.UUID

interface McServerLib : McLib {

    override val commandManager: McCommandManager<McCommand>

    /***
     * @see McServerChannelManager
     */
    val channelManager: McServerChannelManager

    /**
     * Scheduler for executing tasks on the main thread.
     */
    val scheduler: McServerScheduler

    /**
     * Executes the task on the main thread.
     */
    fun executeInMainThread(runnable: Runnable)

    /**
     * Gets a world by name.
     *
     * @param name The world name
     * @return The world or null.
     */
    fun getWorld(name: String): McServerWorld? =
        worlds.firstOrNull { it.name == name }

    /**
     * Gets a world by server-specific instance.
     *
     * The [instance] parameter represents the server-specific server instance:
     *  - For Bukkit `org.bukkit.World`
     *  - For modded servers (Fabric/Forge) `net.minecraft.server.level.ServerLevel`
     *
     * @param instance The server-specific world instance.
     * @return The world.
     */
    fun getWorld(instance: Any): McServerWorld

    /**
     * Gets a collection of all worlds.
     *
     * @return A collection of worlds.
     */
    val worlds: Collection<McServerWorld>

    /**
     * Gets a player by their server-specific instance.
     *
     * The [instance] parameter represents the server-specific player instance:
     *  - For Bukkit: [org.bukkit.entity.Player]
     *  - For modded servers (Fabric/Forge): [net.minecraft.server.level.ServerPlayer]
     *
     * @param instance The server-specific player instance.
     * @return The player.
     */
    fun getPlayerByInstance(instance: Any): McServerPlayer

    /**
     * Gets a player by their name.
     *
     * @param playerName The name of the player.
     * @return The player if found, otherwise `null`.
     */
    fun getPlayerByName(playerName: String): McServerPlayer?

    /**
     * Gets a player by their unique identifier.
     *
     * @param playerId The unique identifier of the player.
     * @return The player if found, otherwise `null`.
     */
    fun getPlayerById(playerId: UUID): McServerPlayer?

    /**
     * Gets a collection of all players.
     *
     * @return A collection of players.
     */
    val players: Collection<McServerPlayer>

    /**
     * Gets a cached game profile by player unique identifier.
     *
     * @param playerId The unique identifier of the player.
     * @return The game profile if found, otherwise `null`.
     */
    fun getGameProfile(playerId: UUID): McGameProfile?

    /**
     * Gets a cached game profile by player name.
     *
     * @param name The name of the player.
     * @return The game profile if found, otherwise `null`.
     */
    fun getGameProfile(name: String): McGameProfile?

    /**
     * Creates a new [McServerEntity] instance of wrapped [instance].
     *
     * The [instance] parameter represents the server-specific entity instance:
     *  - For Bukkit: [org.bukkit.entity.Entity]
     *  - For modded servers (Fabric/Forge): [net.minecraft.world.entity.Entity]
     *
     * @return The entity.
     */
    fun getEntityByInstance(instance: Any): McServerEntity

    /**
     * Gets the bound port of the server.
     *
     * @return The bound port.
     */
    val port: Int

    /**
     * Gets the Minecraft version.
     *
     * @return The Minecraft version.
     */
    val version: String
}
