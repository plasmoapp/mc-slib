package su.plo.slib.minestom.command

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
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.arguments.ArgumentBoolean
import net.minestom.server.command.builder.arguments.ArgumentString
import net.minestom.server.command.builder.arguments.ArgumentWord
import net.minestom.server.command.builder.arguments.number.ArgumentDouble
import net.minestom.server.command.builder.arguments.number.ArgumentFloat
import net.minestom.server.command.builder.arguments.number.ArgumentInteger
import net.minestom.server.command.builder.arguments.number.ArgumentLong
import net.minestom.server.entity.Player
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.style.McTextStyle
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.entity.McEntity
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent
import su.plo.slib.command.AbstractCommandManager
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
            MinecraftServer.getCommandManager().register(command.build().toMinestom())
        }

        registered = true
    }

    override fun getCommandSource(source: Any): McCommandSource  {
        require(source is CommandSender) { "source is not ${CommandSender::class.java}" }

        return if (source is Player) minecraftServer.getPlayerByInstance(source)
        else MinestomDefaultCommandSource(minecraftServer.textConverter, source)
    }

    private fun LiteralCommandNode<*>.toMinestom(): Command {
        val minestomCommand = Command(name)

        val commands = children.filterIsInstance<LiteralCommandNode<*>>()
            .map { it.toMinestom() }
        commands.forEach { minestomCommand.addSubcommand(it) }

        val executor = command?.toMinestom() ?: noopCommandExecutor()

        val arguments = children.filterIsInstance<ArgumentCommandNode<*, *>>()
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

    private fun ArgumentCommandNode<*, *>.toMinestom(): Pair<Argument<*>, CommandExecutor?> {
        val argumentType = type
        val argument = when (argumentType) {
            is BoolArgumentType -> ArgumentBoolean(name)
            is DoubleArgumentType -> ArgumentDouble(name)
            is FloatArgumentType -> ArgumentFloat(name)
            is IntegerArgumentType -> ArgumentInteger(name)
            is LongArgumentType -> ArgumentLong(name)
            is StringArgumentType ->
                when (argumentType.type) {
                    StringArgumentType.StringType.GREEDY_PHRASE -> ArgumentString(name)
                    StringArgumentType.StringType.SINGLE_WORD -> ArgumentWord(name)
                    StringArgumentType.StringType.QUOTABLE_PHRASE -> ArgumentString(name)
                }
            else -> (type as MinestomArgumentType<*>).argumentBuilder(name)
        }
        val executor = command?.toMinestom()

        return argument to executor
    }

    private fun com.mojang.brigadier.Command<*>.toMinestom(): CommandExecutor =
        object : CommandExecutor {
            override fun apply(
                sender: CommandSender,
                context: net.minestom.server.command.builder.CommandContext,
            ) {
                val source = getCommandSource(sender)

                @Suppress("UNCHECKED_CAST")
                val brigadierContext = CommandContext(
                    MinestomBrigadierSource(sender, source, source as? McEntity),
                    context.input,
                    context.map.mapValues { ParsedArgument(0, 0, it.value) },
                    this@toMinestom as com.mojang.brigadier.Command<Any>,
                    null,
                    emptyList(),
                    StringRange(0, 0),
                    null,
                    null,
                    false,
                )

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
}
