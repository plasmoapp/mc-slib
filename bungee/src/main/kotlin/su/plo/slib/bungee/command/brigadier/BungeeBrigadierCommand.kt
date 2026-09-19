package su.plo.slib.bungee.command.brigadier

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.tree.LiteralCommandNode
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.logging.McLogger
import su.plo.slib.bungee.command.BungeeCommandManager
import su.plo.slib.command.brigadier.parseException
import su.plo.slib.command.brigadier.sendFailure
import su.plo.slib.command.brigadier.sendParseFailure

class BungeeBrigadierCommand(
    private val commandManager: BungeeCommandManager,
    private val logger: McLogger,
    private val command: LiteralCommandNode<McBrigadierSource>,
    aliases: Collection<String> = emptyList(),
) : Command(command.literal, null, *aliases.toTypedArray()), TabExecutor {
    private val dispatcher = CommandDispatcher<McBrigadierSource>()

    init {
        dispatcher.root.addChild(command)
    }

    override fun execute(sender: CommandSender, arguments: Array<out String>) {
        val context = BungeeBrigadierSource(commandManager.getCommandSource(sender), instance = sender)
        val input = listOf(command.literal, *arguments).joinToString(" ")

        try {
            val parseResults = dispatcher.parse(input, context)
            val parseException = parseResults.parseException()
            if (parseException != null) {
                context.source.sendParseFailure(parseException, input)
                return
            }

            dispatcher.execute(parseResults)
        } catch (e: CommandSyntaxException) {
            context.source.sendFailure(e)
        } catch (e: Exception) {
            logger.error("Failed to execute command /{}", input, e)
            context.source.sendFailure(McTextComponent.translatable("command.failed"))
        }
    }

    override fun onTabComplete(sender: CommandSender, arguments: Array<out String>): Iterable<String> {
        val context = BungeeBrigadierSource(commandManager.getCommandSource(sender), instance = sender)
        val input = listOf(command.literal, *arguments).joinToString(" ")

        return dispatcher.getCompletionSuggestions(
            dispatcher.parse(input, context),
            input.length,
        )
            .thenApply { suggestions -> suggestions.list.map { it.text } }
            .get()
    }
}
