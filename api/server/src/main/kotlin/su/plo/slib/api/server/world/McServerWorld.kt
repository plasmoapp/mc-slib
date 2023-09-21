package su.plo.slib.api.server.world

import su.plo.slib.api.server.entity.McServerEntity

interface McServerWorld {

    /**
     * @return world key
     */
    val key: String

    /**
     * Sends game event to the world
     *
     * If the given [gameEvent] is invalid, minecraft:step will be sent by default
     *
     * @since minecraft 1.19?
     *
     * @param gameEvent [Minecraft Game Event](https://minecraft.fandom.com/wiki/Sculk_Sensor#Redstone_emission),
     * e.g **minecraft:step**
     */
    fun sendGameEvent(entity: McServerEntity, gameEvent: String)

    /**
     * Gets the server's implementation instance
     *
     *  * `org.bukkit.World` for bukkit
     *  * `net.minecraft.server.level.ServerLevel` for mods (fabric/forge)
     *
     * @return server's implementation object
     */
    fun <T> getInstance(): T
}
