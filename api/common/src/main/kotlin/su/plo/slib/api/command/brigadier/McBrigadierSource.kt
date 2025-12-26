package su.plo.slib.api.command.brigadier

import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.entity.McEntity

interface McBrigadierSource {
    /**
     * Gets the command source that initiated/triggered the execution of a command.
     */
    val source: McCommandSource

    /**
     * Gets the entity executing this command.
     */
    val executor: McEntity?

    /**
     * Gets the server's implementation instance for this source.
     *
     * The return type may vary depending on the server platform:
     *   - For servers (Paper/Fabric/Forge/NeoForge): [net.minecraft.commands.CommandSourceStack]
     *   - For Minestom: [net.minestom.server.command.CommandSender]
     *   - For BungeeCord: [net.md_5.bungee.api.CommandSender]
     *   - For Velocity: [com.velocitypowered.api.command.CommandSource]
     *
     * @return The server's implementation object associated with this source.
     * @param T The expected type of the server's implementation instance.
     */
    fun <T> getInstance(): T
}
