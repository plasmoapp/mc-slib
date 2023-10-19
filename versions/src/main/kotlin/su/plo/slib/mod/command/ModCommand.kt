package su.plo.slib.mod.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import net.minecraft.commands.CommandSourceStack
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.server.McServerLib
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate

class ModCommand(
    private val minecraftServer: McServerLib,
    private val commandManager: ModCommandManager,
    private val command: McCommand
) : Command<CommandSourceStack>, Predicate<CommandSourceStack>, SuggestionProvider<CommandSourceStack> {

    fun register(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        label: String
    ): LiteralCommandNode<CommandSourceStack> {
        val root = dispatcher.root
        val literal = LiteralArgumentBuilder.literal<CommandSourceStack>(label)
            .requires(this)
            .executes(this)
            .build()
        val defaultArgs =
            RequiredArgumentBuilder.argument<CommandSourceStack, String>("args", StringArgumentType.greedyString())
                .suggests(this)
                .executes(this)
                .build()
        literal.addChild(defaultArgs)

        root.addChild(literal)
        return literal
    }

    @Throws(CommandSyntaxException::class)
    override fun run(context: CommandContext<CommandSourceStack>): Int {
        val source = commandManager.getCommandSource(context.source)

        val spaceIndex = context.input.indexOf(' ')
        val args = if (spaceIndex >= 0) {
            context.input.substring(spaceIndex + 1).split(" ").toTypedArray()
        } else {
            emptyArray()
        }

        if (!command.hasPermission(source, args)) {
            source.sendMessage(minecraftServer.permissionManager.noPermissionMessage)
            return 1
        }

        try {
            command.execute(source, args)
        } catch (e: Exception) {
            // todo: logging
//            BaseVoice.LOGGER.error("Error while executing command", e)
            throw e
        }
        return 1
    }

    @Throws(CommandSyntaxException::class)
    override fun getSuggestions(
        context: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val spaceIndex = context.input.indexOf(' ')
        val args = context.input.substring(spaceIndex + 1).split(" ").toTypedArray()
        val results = command.suggest(commandManager.getCommandSource(context.source), args)

        // Defaults to sub nodes, but we have just one giant args node, so offset accordingly
        val newBuilder = builder.createOffset(builder.input.lastIndexOf(' ') + 1)

        for (s in results) {
            newBuilder.suggest(s)
        }

        return newBuilder.buildFuture()
    }

    override fun test(source: CommandSourceStack) =
        command.hasPermission(commandManager.getCommandSource(source), null)
}
