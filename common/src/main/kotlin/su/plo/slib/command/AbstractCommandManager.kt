package su.plo.slib.command

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Maps
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.logging.McLoggerFactory

abstract class AbstractCommandManager<T : McCommand> : McCommandManager<T> {
    private val logger = McLoggerFactory.createLogger("CommandManager")

    protected val commandByName: MutableMap<String, T> = Maps.newHashMap()
    protected var brigadierCommands: MutableList<LiteralArgumentBuilder<Any>> = mutableListOf()

    protected var registered = false

    @get:Synchronized
    override val registeredCommands: Map<String, McCommand>
        get() = ImmutableMap.copyOf<String, McCommand>(commandByName)

    @get:Synchronized
    override val registeredBrigadierCommands: List<LiteralArgumentBuilder<Any>>
        get() = ImmutableList.copyOf(brigadierCommands)

    @Synchronized
    override fun register(command: LiteralArgumentBuilder<Any>) {
        check(!registered) { "register after commands registration is not supported" }
        require(brigadierCommands.none { it.literal == command.literal }) { "Command with name '${command.literal}' already exist" }

        brigadierCommands.add(command)
    }

    @Synchronized
    override fun register(name: String, command: T, vararg aliases: String) {
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

    @Synchronized
    override fun clear() {
        commandByName.clear()
        registered = false
    }

    protected fun registerCommands(register: (String, T) -> Unit) {
        commandByName.forEach { (name, command) ->
            register(name, command)
            logger.info("Command '$name' registered")
        }
    }

    protected fun registerBrigadierCommands(register: (LiteralArgumentBuilder<Any>) -> Unit) {
        brigadierCommands.forEach { command ->
            register(command)
            logger.info("Command '${command.literal}' registered")
        }
    }
}
