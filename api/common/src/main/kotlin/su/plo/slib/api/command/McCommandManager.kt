package su.plo.slib.api.command

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Maps

/**
 * Manages universal commands for multiple server implementations.
 *
 * This class managing commands that are independent of the server implementation (e.g., Paper, Forge, Fabric).
 * These universal commands work across different server types.
 *
 * @param T The type of commands managed by this manager.
 */
abstract class McCommandManager<T : McCommand> {

    protected val commandByName: MutableMap<String, T> = Maps.newHashMap()

    protected var registered = false

    /**
     * Registers a command with its name and optional aliases.
     *
     * @param name     The primary name of the command.
     * @param command  The instance of the command to register.
     * @param aliases  Optional alias names for the command.
     * @throws IllegalStateException If attempting to register commands after commands have already been registered.
     * @throws IllegalArgumentException If a command with the same name or alias already exists.
     */
    @Synchronized
    fun register(name: String, command: T, vararg aliases: String) {
        check(!registered) { "register after commands registration is not supported" }
        require(!commandByName.containsKey(name)) { "Command with name '$name' already exist" }

        for (alias in aliases) {
            require(!commandByName.containsKey(alias)) { "Command with name '$alias' already exist" }
        }

        commandByName[name] = command
        for (alias in aliases) {
            commandByName[alias] = command
        }
    }

    /**
     * Retrieves a read-only map of registered commands.
     *
     * @return A map containing the registered commands with their names as keys.
     */
    @get:Synchronized
    val registeredCommands: Map<String, McCommand>
        get() = ImmutableMap.copyOf<String, McCommand>(commandByName)

    /**
     * Clears all registered commands and resets the registration state.
     */
    @Synchronized
    fun clear() {
        commandByName.clear()
        registered = false
    }

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
}
