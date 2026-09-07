package su.plo.slib.api.command.brigadier

import su.plo.slib.api.chat.component.McTextComponent
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
     * Checks if the source suppresses command feedback.
     *
     * Vanilla sets this for commands running with suppressed output, e.g. inside a datapack function.
     *
     * Always `false` on platforms without a command source stack (Minestom, proxies).
     */
    val isSilent: Boolean
        get() = false

    /**
     * Sends command feedback to the [source], unless the source [isSilent].
     *
     * This is the counterpart of vanilla's `CommandSourceStack#sendSuccess`.
     * Send through [source] directly to reach the sender regardless of the flag.
     *
     * @param text The chat component to send.
     */
    fun sendFeedback(text: McTextComponent) {
        if (!isSilent) source.sendMessage(text)
    }

    /**
     * Sends command feedback to the [source], unless the source [isSilent].
     *
     * @param text The string message to send.
     */
    fun sendFeedback(text: String) {
        sendFeedback(McTextComponent.literal(text))
    }

    /**
     * Gets the server's implementation instance for this source.
     *
     * The return type may vary depending on the server platform:
     *   - For servers (Paper/Fabric/NeoForge): [net.minecraft.commands.CommandSourceStack]
     *   - For Minestom: [net.minestom.server.command.CommandSender]
     *   - For BungeeCord: [net.md_5.bungee.api.CommandSender]
     *   - For Velocity: [com.velocitypowered.api.command.CommandSource]
     *
     * @return The server's implementation object associated with this source.
     * @param T The expected type of the server's implementation instance.
     */
    fun <T> getInstance(): T
}
