package su.plo.slib.minestom.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.ParsedArgument
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.command.ArgumentParserType
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command as MinestomCommand
import net.minestom.server.command.builder.CommandContext as MinestomCommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.condition.CommandCondition
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import net.minestom.server.command.builder.suggestion.SuggestionCallback
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.entity.Player
import net.minestom.server.network.NetworkBuffer
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.style.McTextStyle
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.CustomArgumentType
import su.plo.slib.api.command.brigadier.McBrigadierRegistry
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.entity.McEntity
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent
import su.plo.slib.command.AbstractCommandManager
import su.plo.slib.command.brigadier.CustomArgumentCommandNode
import su.plo.slib.command.brigadier.applyEach
import su.plo.slib.command.brigadier.collectBrigadierCommands
import su.plo.slib.command.brigadier.proxied
import su.plo.slib.command.brigadier.sendFailure
import su.plo.slib.command.brigadier.sendParseFailure
import su.plo.slib.minestom.command.brigadier.MinestomArgumentType
import su.plo.slib.minestom.command.brigadier.MinestomBrigadierSource
import su.plo.slib.minestom.command.brigadier.withParsingSender
import java.util.function.Predicate

class MinestomCommandManager(
    private val minecraftServer: McServerLib,
) : AbstractCommandManager<McCommand>(minecraftServer.baseLogger) {

    // Minestom discards the thrown ArgumentSyntaxException's rich info when building its
    // InvalidCommand, and the per-argument ArgumentCallback is orphan API that never fires.
    // We stash the original CommandSyntaxException here so the defaultExecutor (which does fire
    // on parse errors) can render the translatable message.
    private val pendingParseError = ThreadLocal<PendingParseError>()

    @Synchronized
    fun registerCommands() {
        McServerCommandsRegisterEvent.invoker.onCommandsRegister(this, minecraftServer)

        registerCommands { name, command, _ ->
            val cmd = MinestomCommand(name)
            cmd.setDefaultExecutor { sender, context ->
                val source = getCommandSource(sender)
                val args = context.map.map { it.value.toString() }.toTypedArray()
                if (!command.hasPermission(source, args)) {
                    source.sendMessage(minecraftServer.permissionManager.noPermissionMessage)
                    return@setDefaultExecutor
                }

                command.execute(source, args)
            }
            MinecraftServer.getCommandManager().register(cmd)
        }

        collectBrigadierCommands(McBrigadierRegistry.Phase.RUNTIME)
            .applyEach(logger, logRegisteredCommands) { (node, _, aliases) ->
                MinecraftServer.getCommandManager().register(
                    node.proxied(
                        logger,
                        { it },
                        { it },
                    ).toMinestom(aliases)
                )
            }

        registered = true
    }

    override fun getCommandSource(source: Any): McCommandSource {
        require(source is CommandSender) { "source is not ${CommandSender::class.java}" }

        return if (source is Player) minecraftServer.getPlayerByInstance(source)
        else MinestomDefaultCommandSource(minecraftServer.textConverter, source)
    }

    private fun LiteralCommandNode<McBrigadierSource>.toMinestom(
        aliases: Collection<String> = emptyList(),
        pathRequirement: Predicate<McBrigadierSource> = requirement,
    ): MinestomCommand {
        val minestomCommand = MinestomCommand(name, *aliases.toTypedArray())
        minestomCommand.condition = requirement.toMinestomCondition()

        children.filterIsInstance<LiteralCommandNode<McBrigadierSource>>()
            .forEach { minestomCommand.addSubcommand(it.toMinestom(pathRequirement = pathRequirement.and(it.requirement))) }

        val literalExecutor = command?.toMinestom()

        children.filterIsInstance<ArgumentCommandNode<McBrigadierSource, *>>()
            .forEach { registerArgumentSyntaxes(minestomCommand, it, emptyList(), null, pathRequirement, literalExecutor) }

        minestomCommand.defaultExecutor = defaultCommandExecutor(literalExecutor)

        return minestomCommand
    }

    private fun registerArgumentSyntaxes(
        minestomCommand: MinestomCommand,
        node: ArgumentCommandNode<McBrigadierSource, *>,
        prefix: List<Argument<*>>,
        prefixRequirement: Predicate<McBrigadierSource>?,
        parentPathRequirement: Predicate<McBrigadierSource>,
        fallbackExecutor: CommandExecutor?,
    ) {
        val pathRequirement = parentPathRequirement.and(node.requirement)
        val (argument, executor) = node.toMinestom(pathRequirement)
        val pathSoFar = prefix + argument
        val requirement = prefixRequirement?.and(node.requirement) ?: node.requirement
        val argDescendants = node.children.filterIsInstance<ArgumentCommandNode<McBrigadierSource, *>>()
        val hasSyntax = executor != null || argDescendants.isEmpty()

        if (hasSyntax) {
            minestomCommand.addConditionalSyntax(
                requirement.toMinestomCondition(),
                executor ?: fallbackExecutor ?: noopCommandExecutor(),
                *pathSoFar.toTypedArray(),
            )
        }

        argDescendants.forEach { child ->
            registerArgumentSyntaxes(
                minestomCommand,
                child,
                pathSoFar,
                if (hasSyntax) null else requirement,
                pathRequirement,
                fallbackExecutor,
            )
        }
    }

    private fun Predicate<McBrigadierSource>.toMinestomCondition(): CommandCondition =
        CommandCondition { sender, commandString ->
            if (test(sender.toBrigadierSource())) return@CommandCondition true

            if (commandString != null) pendingParseError.remove()
            false
        }

    private fun noopCommandExecutor(): CommandExecutor =
        CommandExecutor { _, _ -> }

    private fun defaultCommandExecutor(fallback: CommandExecutor?): CommandExecutor =
        CommandExecutor { sender, context ->
            val error = pendingParseError.get()
            if (error != null) {
                pendingParseError.remove()
                getCommandSource(sender).sendParseFailure(error.inCommand(context), context.input)
                return@CommandExecutor
            }
            fallback?.apply(sender, context)
        }

    private fun ArgumentType<*>.toMinestomParserType(): ArgumentParserType =
        when (this) {
            is BoolArgumentType -> ArgumentParserType.BOOL
            is DoubleArgumentType -> ArgumentParserType.DOUBLE
            is FloatArgumentType -> ArgumentParserType.FLOAT
            is IntegerArgumentType -> ArgumentParserType.INTEGER
            is LongArgumentType -> ArgumentParserType.LONG
            is StringArgumentType -> ArgumentParserType.STRING
            is CustomArgumentType<*, *> -> nativeType.toMinestomParserType()
            else -> throw IllegalArgumentException("Invalid argument type: $this")
        }

    private fun <T> ArgumentCommandNode<McBrigadierSource, T>.toMinestom(
        pathRequirement: Predicate<McBrigadierSource>,
    ): Pair<Argument<T>, CommandExecutor?> {
        val argumentType = type
        val executor = command?.toMinestom()
        val customNode = this as? CustomArgumentCommandNode<*, *, *>

        if (customNode == null && argumentType is MinestomArgumentType<T>) {
            val nativeArgument = argumentType.argumentBuilder.invoke(name)
            nativeArgument.suggestionCallback?.let { nativeArgument.suggestionCallback = it.requiring(pathRequirement) }

            return nativeArgument to executor
        }

        val nativeArgument = (argumentType as? MinestomArgumentType<*>)?.argumentBuilder?.invoke(name)

        val minestomArgument = object : Argument<T>(
            name,
            nativeArgument?.allowSpace() ?: false,
            nativeArgument?.useRemaining() ?: false,
        ) {
            @Suppress("UNCHECKED_CAST")
            override fun parse(sender: CommandSender, input: String): T {
                pendingParseError.remove()
                return try {
                    if (customNode != null) {
                        withParsingSender(sender) {
                            customNode.customArgumentType.parse(StringReader(input)) as T
                        }
                    } else {
                        argumentType.parse(StringReader(input))
                    }
                } catch (e: CommandSyntaxException) {
                    pendingParseError.set(PendingParseError(e, input))
                    throw ArgumentSyntaxException(e.message, input, -1)
                }
            }

            override fun parser(): ArgumentParserType =
                nativeArgument?.parser() ?: argumentType.toMinestomParserType()

            override fun nodeProperties(): ByteArray? {
                nativeArgument?.let { return it.nodeProperties() }

                if (argumentType is StringArgumentType) {
                    return NetworkBuffer.makeArray(NetworkBuffer.VAR_INT, argumentType.type.ordinal)
                }

                return super.nodeProperties()
            }
        }

        val suggestionCallback = SuggestionCallback { sender, context, suggestion ->
            val brigadierContext = context.toBrigadier(sender, command)
            val suggestions = listSuggestions(brigadierContext, SuggestionsBuilder(context.input, 0)).get()

            suggestions.list.forEach {
                suggestion.addEntry(
                    SuggestionEntry(it.text, it.tooltip?.string?.let(Component::text))
                )
            }
        }
        minestomArgument.suggestionCallback = suggestionCallback.requiring(pathRequirement)

        return minestomArgument to executor
    }

    // minestom never checks conditions when suggesting, so a client can ask for suggestions of a node it can't use
    private fun SuggestionCallback.requiring(requirement: Predicate<McBrigadierSource>): SuggestionCallback =
        SuggestionCallback { sender, context, suggestion ->
            if (requirement.test(sender.toBrigadierSource())) apply(sender, context, suggestion)
        }

    private fun com.mojang.brigadier.Command<McBrigadierSource>.toMinestom(): CommandExecutor =
        CommandExecutor { sender, context ->
            pendingParseError.remove()
            val source = getCommandSource(sender)
            val brigadierContext = context.toBrigadier(sender, this@toMinestom)

            try {
                this@toMinestom.run(brigadierContext)
            } catch (e: CommandSyntaxException) {
                source.sendFailure(e)
            } catch (e: Exception) {
                source.sendMessage(
                    McTextComponent.literal(e.message ?: "Unknown error").withStyle(McTextStyle.RED)
                )
            }
        }

    private fun MinestomCommandContext.toBrigadier(
        sender: CommandSender,
        command: com.mojang.brigadier.Command<McBrigadierSource>?,
    ): CommandContext<McBrigadierSource> =
        CommandContext(
            sender.toBrigadierSource(),
            input,
            map.mapValues { ParsedArgument(0, 0, it.value) },
            command,
            null,
            emptyList(),
            StringRange(0, 0),
            null,
            null,
            false,
        )

    private fun CommandSender.toBrigadierSource(): McBrigadierSource {
        val source = getCommandSource(this)
        return MinestomBrigadierSource(source, source as? McEntity, this)
    }

    private class PendingParseError(
        val exception: CommandSyntaxException,
        val argumentInput: String,
    ) {
        fun inCommand(context: MinestomCommandContext): CommandSyntaxException {
            if (exception.input != argumentInput) return exception

            val offset = context.failedArgumentOffset()
            if (!context.input.startsWith(argumentInput, offset)) return exception

            return CommandSyntaxException(
                exception.type,
                exception.rawMessage,
                context.input,
                offset + exception.cursor,
            )
        }

        private fun MinestomCommandContext.failedArgumentOffset(): Int =
            map.keys.sumOf { id ->
                val raw = getRaw(id)
                if (raw.isNullOrEmpty()) 0 else raw.length + 1
            } + commandName.length + 1
    }
}
