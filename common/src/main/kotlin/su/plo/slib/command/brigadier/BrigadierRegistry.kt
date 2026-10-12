package su.plo.slib.command.brigadier

import com.mojang.brigadier.tree.LiteralCommandNode
import su.plo.slib.api.command.brigadier.McBrigadierRegistry
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.event.command.McBrigadierCommandsRegisterEvent
import su.plo.slib.api.logging.McLogger
import su.plo.slib.api.logging.McLoggerFactory

private val logger = McLoggerFactory.createLogger("slib")

data class RegisteredBrigadierCommand(
    val node: LiteralCommandNode<McBrigadierSource>,
    val description: String?,
    val aliases: List<String>,
    val registrant: McBrigadierCommandsRegisterEvent.Callback,
)

class BrigadierRegistry(
    val phase: McBrigadierRegistry.Phase,
) {
    private val commands = mutableListOf<RegisteredBrigadierCommand>()
    private val takenNames = mutableSetOf<String>()

    val registeredCommands: List<RegisteredBrigadierCommand>
        get() = commands.toList()

    fun collectFrom(registrant: McBrigadierCommandsRegisterEvent.Callback) {
        try {
            registrant.onCommandsRegister(RegistrantScope(registrant))
        } catch (e: Throwable) {
            logger.error("Failed to build brigadier commands in {}", registrant, e)
        }
    }

    @Synchronized
    private fun register(
        registrant: McBrigadierCommandsRegisterEvent.Callback,
        command: LiteralCommandNode<McBrigadierSource>,
        description: String?,
        aliases: Collection<String>,
    ) {
        val names = listOf(command.literal) + aliases
        names.forEach { name ->
            require(name !in takenNames) { "Command with name '$name' already exist" }
        }

        takenNames.addAll(names)
        commands.add(RegisteredBrigadierCommand(command, description, aliases.toList(), registrant))
    }

    private inner class RegistrantScope(
        private val registrant: McBrigadierCommandsRegisterEvent.Callback,
    ) : McBrigadierRegistry {
        override val phase: McBrigadierRegistry.Phase
            get() = this@BrigadierRegistry.phase

        override fun register(
            command: LiteralCommandNode<McBrigadierSource>,
            description: String?,
            aliases: Collection<String>,
        ) = register(registrant, command, description, aliases)
    }
}

fun collectBrigadierCommands(
    phase: McBrigadierRegistry.Phase,
    shouldCollect: (McBrigadierCommandsRegisterEvent.Callback) -> Boolean = { true },
): BrigadierRegistry =
    BrigadierRegistry(phase).also { registry ->
        McBrigadierCommandsRegisterEvent.listeners
            .filter(shouldCollect)
            .forEach(registry::collectFrom)
    }

fun BrigadierRegistry.applyEach(
    logger: McLogger,
    logRegistered: Boolean,
    register: (RegisteredBrigadierCommand) -> Unit,
) {
    registeredCommands.forEach { command ->
        try {
            register(command)
            if (logRegistered) logger.info("Command '{}' registered", command.node.literal)
        } catch (e: Throwable) {
            logger.error("Failed to register command '{}'", command.node.literal, e)
        }
    }
}
