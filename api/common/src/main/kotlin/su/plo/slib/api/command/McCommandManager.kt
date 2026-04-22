package su.plo.slib.api.command

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import su.plo.slib.api.command.brigadier.McBrigadierSource

/**
 * Manages universal commands for multiple server implementations.
 *
 * This class managing commands that are independent of the server implementation (e.g., Paper, Forge, Fabric).
 * These universal commands work across different server types.
 *
 * @param T The type of commands managed by this manager.
 */
abstract class McCommandManager<T : McCommand> {

    /**
     * Retrieves a read-only map of registered commands.
     *
     * @return A map containing the registered commands with their names as keys.
     */
    abstract val registeredCommands: Map<String, McCommand>

    /**
     * Retrieves a read-only map of registered brigadier commands.
     *
     * @return A list containing the registered commands with their names as keys.
     */
    abstract val registeredBrigadierCommands: List<LiteralCommandNode<McBrigadierSource>>

    /**
     * Registers a brigadier command.
     *
     * @param command  The instance of the command to register.
     * @throws IllegalStateException If attempting to register commands after commands have already been registered.
     * @throws IllegalArgumentException If a command with the same name or alias already exists.
     */
    fun register(command: LiteralArgumentBuilder<McBrigadierSource>) {
        register(command.build())
    }

    /**
     * Registers a brigadier command.
     *
     * @param command  The instance of the command to register.
     * @throws IllegalStateException If attempting to register commands after commands have already been registered.
     * @throws IllegalArgumentException If a command with the same name or alias already exists.
     */
    abstract fun register(command: LiteralCommandNode<McBrigadierSource>)

    /**
     * Registers a command with its name and optional aliases.
     *
     * @param name     The primary name of the command.
     * @param command  The instance of the command to register.
     * @param aliases  Optional alias names for the command.
     * @throws IllegalStateException If attempting to register commands after commands have already been registered.
     * @throws IllegalArgumentException If a command with the same name or alias already exists.
     */
    abstract fun register(name: String, command: T, vararg aliases: String)

    /**
     * Clears all registered commands and resets the registration state.
     */
    abstract fun clear()

    /**
     * Gets a command source by server-specific instance.
     *
     * The [source] parameter represents the server-specific command source instance:
     *  - For Velocity `com.velocitypowered.api.command.CommandSource`
     *  - For BungeeCord `// todo`
     *
     * @param source The server-specific command source instance.
     * @return A [McCommandSource] instance corresponding to the provided command source instance.
     */
    abstract fun getCommandSource(source: Any): McCommandSource

    companion object {
        @JvmStatic
        fun literal(name: String): LiteralArgumentBuilder<McBrigadierSource> =
            LiteralArgumentBuilder.literal<McBrigadierSource>(name)

        @JvmStatic
        fun <T> argument(name: String, argument: ArgumentType<T>): RequiredArgumentBuilder<McBrigadierSource, T> =
            RequiredArgumentBuilder.argument<McBrigadierSource, T>(name, argument)
    }
}
