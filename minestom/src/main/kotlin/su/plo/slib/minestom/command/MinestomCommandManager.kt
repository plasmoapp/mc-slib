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
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.arguments.minecraft.SuggestionType
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import net.minestom.server.command.builder.suggestion.Suggestion
import net.minestom.server.command.builder.suggestion.SuggestionCallback
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.entity.Player
import net.minestom.server.network.NetworkBuffer
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.style.McTextStyle
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.CustomArgumentType
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.entity.McEntity
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent
import su.plo.slib.command.AbstractCommandManager
import su.plo.slib.command.brigadier.CustomArgumentCommandNode
import su.plo.slib.command.proxied
import su.plo.slib.minestom.command.brigadier.MinestomArgumentType
import su.plo.slib.minestom.command.brigadier.MinestomBrigadierSource

class MinestomCommandManager(
    private val minecraftServer: McServerLib
) : AbstractCommandManager<McCommand>() {

    @Synchronized
    fun registerCommands() {
        McServerCommandsRegisterEvent.invoker.onCommandsRegister(this, minecraftServer)

        registerCommands { name, command ->
            val cmd = Command(name)
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

        registerBrigadierCommands { command ->
            MinecraftServer.getCommandManager().register(
                command.proxied(
                    { it },
                    { it },
                ).toMinestom()
            )
        }

        registered = true
    }

    override fun getCommandSource(source: Any): McCommandSource  {
        require(source is CommandSender) { "source is not ${CommandSender::class.java}" }

        return if (source is Player) minecraftServer.getPlayerByInstance(source)
        else MinestomDefaultCommandSource(minecraftServer.textConverter, source)
    }

    private fun LiteralCommandNode<McBrigadierSource>.toMinestom(): Command {
        val minestomCommand = Command(name)

        val commands = children.filterIsInstance<LiteralCommandNode<McBrigadierSource>>()
            .map { it.toMinestom() }
        commands.forEach { minestomCommand.addSubcommand(it) }

        val executor = command?.toMinestom() ?: noopCommandExecutor()

        val arguments = children.filterIsInstance<ArgumentCommandNode<McBrigadierSource, *>>()
            .map { it.toMinestom() }
        arguments.forEach {(argument, argumentExecutor) ->
            minestomCommand.addSyntax(argumentExecutor ?: executor, argument)
        }

        minestomCommand.defaultExecutor = executor

        return minestomCommand
    }

    private fun noopCommandExecutor(): CommandExecutor =
        object : CommandExecutor {
            override fun apply(
                sender: CommandSender,
                context: net.minestom.server.command.builder.CommandContext,
            ) {
            }
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

    private fun <T> ArgumentCommandNode<McBrigadierSource, T>.toMinestom(): Pair<Argument<T>, CommandExecutor?> {
        val argumentType = type
        val executor = command?.toMinestom()

        if (argumentType is MinestomArgumentType<T>) {
            return argumentType.argumentBuilder.invoke(name) to executor
        }

        val minestomArgument = object : Argument<T>(name) {
            @Suppress("UNCHECKED_CAST")
            override fun parse(sender: CommandSender, input: String): T =
                try {
                    if (this@toMinestom is CustomArgumentCommandNode<*, *, *>) {
                        (customArgumentType.parse(StringReader(input)) as T)
                    } else {
                        argumentType.parse(StringReader(input))
                    }
                } catch (e: CommandSyntaxException) {
                    throw ArgumentSyntaxException(e.message, input, -1)
                }

            override fun parser(): ArgumentParserType =
                argumentType.toMinestomParserType()

            override fun nodeProperties(): ByteArray? {
                if (argumentType is StringArgumentType) {
                    return NetworkBuffer.makeArray(NetworkBuffer.VAR_INT, argumentType.type.ordinal)
                }

                return super.nodeProperties()
            }
        }

        minestomArgument.suggestionCallback = object : SuggestionCallback {
            override fun apply(
                sender: CommandSender,
                context: net.minestom.server.command.builder.CommandContext,
                suggestion: Suggestion,
            ) {
                val brigadierContext = context.toBrigadier(sender, command)
                val suggestions = listSuggestions(brigadierContext, SuggestionsBuilder(context.input, 0)).get()

                suggestions.list.forEach {
                    suggestion.addEntry(
                        SuggestionEntry(it.text, it.tooltip?.string?.let(Component::text))
                    )
                }
            }
        }

        return minestomArgument to executor
    }

    private fun com.mojang.brigadier.Command<McBrigadierSource>.toMinestom(): CommandExecutor =
        object : CommandExecutor {
            override fun apply(
                sender: CommandSender,
                context: net.minestom.server.command.builder.CommandContext,
            ) {
                val source = getCommandSource(sender)

                val brigadierContext = context.toBrigadier(sender, this@toMinestom)

                try {
                    this@toMinestom.run(brigadierContext)
                } catch (e: CommandSyntaxException) {
                    source.sendMessage(
                        McTextComponent.translatable(
                            "command.context.parse_error",
                            McTextComponent.literal(e.rawMessage.string),
                            McTextComponent.literal(e.cursor.toString()),
                            McTextComponent.literal(e.context),
                        ).withStyle(McTextStyle.RED)
                    )
                } catch (e: Exception) {
                    source.sendMessage(
                        McTextComponent.literal(e.message ?: "Unknown error").withStyle(McTextStyle.RED)
                    )
                }
            }
        }

    private fun net.minestom.server.command.builder.CommandContext.toBrigadier(
        sender: CommandSender,
        command: com.mojang.brigadier.Command<McBrigadierSource>?,
    ): CommandContext<McBrigadierSource> {
        val source = getCommandSource(sender)

        return CommandContext(
            MinestomBrigadierSource(source, source as? McEntity, sender),
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
    }
}
