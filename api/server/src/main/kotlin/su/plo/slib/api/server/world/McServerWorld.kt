package su.plo.slib.api.server.world

import su.plo.slib.api.server.entity.McServerEntity

/**
 * Represents a Minecraft server world.
 */
interface McServerWorld {

    /**
     * Gets the platform-specific name of this world.
     *
     * The returned value differs between platforms:
     *   - For Bukkit: the world folder name, e.g. "world_nether"
     *   - For modded servers (Fabric/Forge): the dimension key, e.g. "minecraft:the_nether"
     *   - For Minestom: the instance uuid
     *
     * Prefer [key] for anything that has to be portable between platforms or persisted.
     */
    val name: String

    /**
     * Gets the key that identifies this world, e.g. "minecraft:the_nether".
     *
     * Unlike [name], this is the same value on Bukkit and modded servers for a given dimension.
     *
     * Minestom has no namespaced identity for instances, so it falls back to "minestom:<instance uuid>",
     * which is not stable across restarts.
     */
    val key: String

    /**
     * Sends a game event to the world.
     *
     * If an invalid game event is provided, the default event "minecraft:step" will be sent.
     *
     * [Minecraft Game Event](https://minecraft.fandom.com/wiki/Sculk_Sensor#Redstone_emission)
     *
     * @param entity The entity associated with the game event.
     * @param gameEvent The name of the Minecraft game event to send, e.g., "minecraft:step".
     * @since Minecraft version 1.19
     */
    fun sendGameEvent(entity: McServerEntity, gameEvent: String)

    /**
     * Gets the server's implementation instance for this world.
     *
     * The return type of this method may vary depending on the server platform:
     *   - For Bukkit: [org.bukkit.World]
     *   - For modded servers (Fabric/Forge): [net.minecraft.server.level.ServerLevel]
     *
     * @return The server's implementation object associated with this world.
     * @param T The expected type of the implementation object.
     */
    fun <T> getInstance(): T
}
