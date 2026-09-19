package su.plo.slib.minestom.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.ParseResults
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
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.command.ArgumentParserType
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command as MinestomCommand
import net.minestom.server.command.builder.CommandContext as MinestomCommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.arguments.ArgumentLiteral
import net.minestom.server.command.builder.condition.CommandCondition
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import net.minestom.server.command.builder.suggestion.Suggestion
import net.minestom.server.command.builder.suggestion.SuggestionCallback
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.entity.Player
import net.minestom.server.network.NetworkBuffer
import su.plo.slib.api.chat.component.McTextComponent
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
import su.plo.slib.command.brigadier.copyLiteral
import su.plo.slib.command.brigadier.parseException
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
                        sourceUnwrapper = { it },
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

    private fun LiteralCommandNode<McBrigadierSource>.toMinestom(aliases: Collection<String>): MinestomCommand {
        val dispatcher = CommandDispatcher<McBrigadierSource>()
        dispatcher.root.addChild(this)
        aliases.forEach { dispatcher.root.addChild(copyLiteral(it)) }

        return toMinestom(dispatcher, aliases, requirement)
    }

    private fun LiteralCommandNode<McBrigadierSource>.toMinestom(
        dispatcher: CommandDispatcher<McBrigadierSource>,
        aliases: Collection<String>,
        pathRequirement: Predicate<McBrigadierSource>,
    ): MinestomCommand {
        val minestomCommand = MinestomCommand(name, *aliases.toTypedArray())
        minestomCommand.condition = requirement.toMinestomCondition(dispatcher)

        children.filterIsInstance<LiteralCommandNode<McBrigadierSource>>()
            .forEach {
                minestomCommand.addSubcommand(
                    it.toMinestom(dispatcher, emptyList(), pathRequirement.and(it.requirement))
                )
            }

        children.filterIsInstance<ArgumentCommandNode<McBrigadierSource, *>>()
            .forEach { registerSyntaxes(dispatcher, minestomCommand, it, emptyList(), null, pathRequirement) }

        minestomCommand.defaultExecutor = dispatcher.toMinestomExecutor()

        return minestomCommand
    }

    private fun registerSyntaxes(
        dispatcher: CommandDispatcher<McBrigadierSource>,
        minestomCommand: MinestomCommand,
        node: CommandNode<McBrigadierSource>,
        prefix: List<Argument<*>>,
        prefixRequirement: Predicate<McBrigadierSource>?,
        parentPathRequirement: Predicate<McBrigadierSource>,
    ) {
        val pathRequirement = parentPathRequirement.and(node.requirement)
        val (argument, executor) = node.toMinestom(pathRequirement)
        val pathSoFar = prefix + argument
        val requirement = prefixRequirement?.and(node.requirement) ?: node.requirement
        val hasSyntax = executor != null || node.children.isEmpty()

        if (hasSyntax) {
            minestomCommand.addConditionalSyntax(
                requirement.toMinestomCondition(dispatcher),
                executor ?: dispatcher.toMinestomExecutor(),
                *pathSoFar.toTypedArray(),
            )
        }

        node.children.forEach { child ->
            registerSyntaxes(
                dispatcher,
                minestomCommand,
                child,
                pathSoFar,
                if (hasSyntax) null else requirement,
                pathRequirement,
            )
        }
    }

    private fun Predicate<McBrigadierSource>.toMinestomCondition(
        dispatcher: CommandDispatcher<McBrigadierSource>,
    ): CommandCondition =
        CommandCondition { sender, commandString ->
            if (test(sender.toBrigadierSource())) return@CommandCondition true

            // minestom passes null while building the client command tree, and the input only when executing
            if (commandString != null) {
                dispatcher.parseAs(sender, commandString).parseException()
                    ?.let { getCommandSource(sender).sendParseFailure(it, commandString) }
            }

            false
        }

    private fun CommandDispatcher<McBrigadierSource>.toMinestomExecutor(): CommandExecutor =
        CommandExecutor { sender, context ->
            val source = getCommandSource(sender)
            val parseResults = parseAs(sender, context.input)

            val parseException = parseResults.parseException()
            if (parseException != null) {
                source.sendParseFailure(parseException, context.input)
                return@CommandExecutor
            }

            source.catchingFailures(context.input) { execute(parseResults) }
        }

    private fun CommandDispatcher<McBrigadierSource>.parseAs(
        sender: CommandSender,
        input: String,
    ): ParseResults<McBrigadierSource> =
        withParsingSender(sender) { parse(input, sender.toBrigadierSource()) }

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

    private fun ArgumentType<*>.toMinestomNodeProperties(): ByteArray? =
        when (this) {
            is DoubleArgumentType -> numberProperties(
                minimum.takeIf { it != -Double.MAX_VALUE },
                maximum.takeIf { it != Double.MAX_VALUE },
                NetworkBuffer.DOUBLE,
            )
            is FloatArgumentType -> numberProperties(
                minimum.takeIf { it != -Float.MAX_VALUE },
                maximum.takeIf { it != Float.MAX_VALUE },
                NetworkBuffer.FLOAT,
            )
            is IntegerArgumentType -> numberProperties(
                minimum.takeIf { it != Int.MIN_VALUE },
                maximum.takeIf { it != Int.MAX_VALUE },
                NetworkBuffer.INT,
            )
            is LongArgumentType -> numberProperties(
                minimum.takeIf { it != Long.MIN_VALUE },
                maximum.takeIf { it != Long.MAX_VALUE },
                NetworkBuffer.LONG,
            )
            is StringArgumentType -> NetworkBuffer.makeArray(NetworkBuffer.VAR_INT, type.ordinal)
            is CustomArgumentType<*, *> -> nativeType.toMinestomNodeProperties()
            else -> null
        }

    private fun <T : Any> numberProperties(min: T?, max: T?, type: NetworkBuffer.Type<T>): ByteArray =
        NetworkBuffer.makeArray { buffer ->
            var flags = 0
            if (min != null) flags = flags or 0x01
            if (max != null) flags = flags or 0x02

            buffer.write(NetworkBuffer.BYTE, flags.toByte())
            min?.let { buffer.write(type, it) }
            max?.let { buffer.write(type, it) }
        }

    private fun CommandNode<McBrigadierSource>.toMinestom(
        pathRequirement: Predicate<McBrigadierSource>,
    ): Pair<Argument<*>, CommandExecutor?> =
        when (this) {
            is ArgumentCommandNode<McBrigadierSource, *> -> toMinestomArgument(pathRequirement)
            is LiteralCommandNode<McBrigadierSource> -> ArgumentLiteral(name) to command?.toMinestom()
            else -> throw IllegalArgumentException("Unsupported command node: $this")
        }

    private fun <T> ArgumentCommandNode<McBrigadierSource, T>.toMinestomArgument(
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
            override fun parse(sender: CommandSender, input: String): T =
                try {
                    if (customNode != null) {
                        withParsingSender(sender) {
                            customNode.customArgumentType.parse(StringReader(input)) as T
                        }
                    } else {
                        argumentType.parse(StringReader(input))
                    }
                } catch (e: CommandSyntaxException) {
                    throw ArgumentSyntaxException(e.message, input, -1)
                }

            override fun parser(): ArgumentParserType =
                nativeArgument?.parser() ?: argumentType.toMinestomParserType()

            override fun nodeProperties(): ByteArray? =
                nativeArgument?.nodeProperties() ?: argumentType.toMinestomNodeProperties()
        }

        val suggestionCallback = SuggestionCallback { sender, context, suggestion ->
            val brigadierContext = context.toBrigadier(sender, command)
            val suggestions = listSuggestions(brigadierContext, suggestion.toBrigadier()).get()

            suggestions.list.forEach {
                suggestion.addEntry(
                    SuggestionEntry(it.text, it.tooltip?.string?.let(Component::text))
                )
            }
        }
        minestomArgument.suggestionCallback = suggestionCallback.requiring(pathRequirement)

        return minestomArgument to executor
    }

    private fun Suggestion.toBrigadier(): SuggestionsBuilder {
        val input = input.removeSuffix("\u0000")

        return SuggestionsBuilder(input, (start - 1).coerceIn(0, input.length))
    }

    // minestom never checks conditions when suggesting, so a client can ask for suggestions of a node it can't use
    private fun SuggestionCallback.requiring(requirement: Predicate<McBrigadierSource>): SuggestionCallback =
        SuggestionCallback { sender, context, suggestion ->
            if (requirement.test(sender.toBrigadierSource())) apply(sender, context, suggestion)
        }

    private fun com.mojang.brigadier.Command<McBrigadierSource>.toMinestom(): CommandExecutor =
        CommandExecutor { sender, context ->
            val brigadierContext = context.toBrigadier(sender, this@toMinestom)

            getCommandSource(sender).catchingFailures(context.input) { this@toMinestom.run(brigadierContext) }
        }

    private inline fun McCommandSource.catchingFailures(input: String, block: () -> Unit) {
        try {
            block()
        } catch (e: CommandSyntaxException) {
            sendFailure(e)
        } catch (e: Exception) {
            logger.error("Failed to execute command /{}", input, e)
            sendFailure(McTextComponent.translatable("command.failed"))
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
}
