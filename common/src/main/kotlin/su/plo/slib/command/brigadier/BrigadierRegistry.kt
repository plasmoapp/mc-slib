package su.plo.slib.command.brigadier

import com.mojang.brigadier.tree.LiteralCommandNode
import su.plo.slib.api.command.brigadier.McBrigadierRegistry
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.event.command.McBrigadierCommandsRegisterEvent
import su.plo.slib.api.logging.McLogger

data class RegisteredBrigadierCommand(
    val node: LiteralCommandNode<McBrigadierSource>,
    val description: String?,
    val aliases: List<String>,
)

class BrigadierRegistry(
    override val phase: McBrigadierRegistry.Phase,
) : McBrigadierRegistry {
    private val commands = mutableListOf<RegisteredBrigadierCommand>()

    val registeredCommands: List<RegisteredBrigadierCommand>
        get() = commands.toList()

    @Synchronized
    override fun register(
        command: LiteralCommandNode<McBrigadierSource>,
        description: String?,
        aliases: Collection<String>,
    ) {
        require(commands.none { it.node.literal == command.literal }) {
            "Command with name '${command.literal}' already exist"
        }

        commands.add(RegisteredBrigadierCommand(command, description, aliases.toList()))
    }
}

fun collectBrigadierCommands(phase: McBrigadierRegistry.Phase): BrigadierRegistry =
    BrigadierRegistry(phase).also { McBrigadierCommandsRegisterEvent.invoker.onCommandsRegister(it) }

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
