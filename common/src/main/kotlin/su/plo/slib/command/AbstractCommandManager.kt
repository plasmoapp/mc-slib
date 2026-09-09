package su.plo.slib.command

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Maps
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.logging.McLogger
import su.plo.slib.api.logging.McLoggerFactory

abstract class AbstractCommandManager<T : McCommand>(
    baseLogger: McLogger
) : McCommandManager<T>() {
    protected val logger = McLoggerFactory.createLogger(baseLogger, "CommandManager")

    protected val commandByName: MutableMap<String, T> = Maps.newHashMap()
    protected val namespaceByName: MutableMap<String, String> = Maps.newHashMap()

    protected var registered = false

    @get:Synchronized
    override val registeredCommands: Map<String, McCommand>
        get() = ImmutableMap.copyOf<String, McCommand>(commandByName)

    @Synchronized
    override fun register(name: String, command: T, vararg aliases: String) {
        register(commandNamespace, name, command, *aliases)
    }

    @Synchronized
    override fun register(namespace: String, name: String, command: T, vararg aliases: String) {
        check(!registered) { "register after commands registration is not supported" }
        require(!commandByName.containsKey(name)) { "Command with name '$name' already exist" }

        for (alias in aliases) {
            require(!commandByName.containsKey(alias)) { "Command with name '$alias' already exist" }
        }

        commandByName[name] = command
        namespaceByName[name] = namespace
        for (alias in aliases) {
            commandByName[alias] = command
            namespaceByName[alias] = namespace
        }
    }

    @Synchronized
    override fun clear() {
        commandByName.clear()
        namespaceByName.clear()
        registered = false
    }

    protected fun registerCommands(register: (String, T, String) -> Unit) {
        commandByName.forEach { (name, command) ->
            val namespace = namespaceByName[name] ?: commandNamespace
            register(name, command, namespace)
            if (logRegisteredCommands) logger.info("Command '$name' registered")
        }
    }
}


