package su.plo.slib.api.command

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Maps

/**
 * Manages universal commands
 *
 * Universal commands are server implementation independent, so they will work on Paper/Forge/Fabric/etc.
 */
abstract class McCommandManager<T : McCommand> {

    protected val commandByName: MutableMap<String, T> = Maps.newHashMap()

    protected var registered = false

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

    @get:Synchronized
    val registeredCommands: Map<String, McCommand>
        get() = ImmutableMap.copyOf<String, McCommand>(commandByName)

    @Synchronized
    fun clear() {
        commandByName.clear()
        registered = false
    }

    abstract fun getCommandSource(source: Any): McCommandSource
}
