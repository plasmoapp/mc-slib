package su.plo.slib.bungee.command.brigadier

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.style.McTextStyle
import su.plo.slib.bungee.command.BungeeCommandManager

class BungeeBrigadierCommand(
    private val commandManager: BungeeCommandManager,
    private val command: LiteralArgumentBuilder<Any>,
) : Command(command.literal), TabExecutor {
    private val dispatcher = CommandDispatcher<BungeeBrigadierSource>()

    init {
        @Suppress("UNCHECKED_CAST")
        dispatcher.register(command as LiteralArgumentBuilder<BungeeBrigadierSource>)
    }

    override fun execute(sender: CommandSender, arguments: Array<out String>) {
        val context = BungeeBrigadierSource(commandManager.getCommandSource(sender))
        val input = listOf(command.literal, *arguments).joinToString(" ")

        try {
            dispatcher.execute(input, context)
        } catch (e: CommandSyntaxException) {
            context.source.sendMessage(
                McTextComponent.translatable(
                    "command.context.parse_error",
                    McTextComponent.literal(e.rawMessage.string),
                    McTextComponent.literal(e.cursor.toString()),
                    McTextComponent.literal(e.context),
                ).withStyle(McTextStyle.RED)
            )
        } catch (e: Exception) {
            context.source.sendMessage(
                McTextComponent.literal(e.message ?: "Unknown error").withStyle(McTextStyle.RED)
            )
        }
    }

    override fun onTabComplete(sender: CommandSender, arguments: Array<out String>): Iterable<String> {
        val context = BungeeBrigadierSource(commandManager.getCommandSource(sender))
        val input = listOf(command.literal, *arguments).joinToString(" ")

        return dispatcher.getCompletionSuggestions(
            dispatcher.parse(input, context),
            input.length,
        )
            .thenApply { suggestions -> suggestions.list.map { it.text } }
            .get()
    }
}
