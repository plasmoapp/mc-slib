package su.plo.slib.server.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.brigadier.CustomArgumentType
import su.plo.slib.api.command.brigadier.McTextMessage
import su.plo.slib.server.INVALID_ARGUMENT_KEY
import java.util.concurrent.CompletableFuture

class TranslatedArgumentType : CustomArgumentType<String, String> {
    override val nativeType: ArgumentType<String> = StringArgumentType.word()

    override fun useNativeSuggestions(): Boolean = false

    override fun parse(reader: StringReader): String {
        val input = reader.readUnquotedString()
        if (input != ACCEPTED_VALUE) throw invalidValue.createWithContext(reader, input)

        return input
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> = Suggestions.empty()

    companion object {
        private const val ACCEPTED_VALUE = "ok"

        private val invalidValue = DynamicCommandExceptionType { value ->
            McTextMessage.of(
                McTextComponent.translatable(INVALID_ARGUMENT_KEY, McTextComponent.literal(value.toString()))
            )
        }
    }
}
