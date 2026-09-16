package su.plo.slib.api.command.brigadier

import com.mojang.brigadier.exceptions.CommandSyntaxException
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.style.McTextStyle
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
     * Checks if the platform library is initialized for this source.
     *
     * This is `false` only while a command registered in [McBrigadierRegistry.Phase.BOOTSTRAP],
     * i.e. when datapack functions are loaded.
     * In that state [source] answers [McCommandSource.hasPermission] with `true`,
     * reports every permission as undefined, discards anything sent to it,
     * and [executor] is always `null`.
     *
     * `requires` predicates run during parsing,
     * so they must not depend on state that only exists once the plugin or mod is initialized.
     */
    val isBound: Boolean
        get() = true

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
     * Sends the exception's message to the [source] as a command failure, unless the source [isSilent].
     *
     * This is the counterpart of vanilla's `CommandSourceStack#handleError`:
     * only the raw message is sent, without the input context.
     *
     * @param exception The exception to report.
     */
    fun sendFailure(exception: CommandSyntaxException) {
        if (isSilent) return

        val message = (exception.rawMessage as? McTextMessage)?.component
            ?: McTextComponent.literal(exception.rawMessage.string)

        source.sendMessage(McTextComponent.empty().append(message).withStyle(McTextStyle.RED))
    }

    /**
     * Gets the server's implementation instance for this source.
     *
     * The return type may vary depending on the server platform:
     *   - For Paper: [io.papermc.paper.command.brigadier.CommandSourceStack]
     *   - For modded servers (Fabric/NeoForge): [net.minecraft.commands.CommandSourceStack]
     *   - For Minestom: [net.minestom.server.command.CommandSender]
     *   - For BungeeCord: [net.md_5.bungee.api.CommandSender]
     *   - For Velocity: [com.velocitypowered.api.command.CommandSource]
     *
     * @return The server's implementation object associated with this source.
     * @param T The expected type of the server's implementation instance.
     */
    fun <T> getInstance(): T
}
