package su.plo.slib.server.command

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import su.plo.slib.api.command.brigadier.CustomArgumentType
import java.util.UUID
import java.util.concurrent.CompletableFuture

class UuidArgumentType : CustomArgumentType<UUID, String> {
    override val nativeType: ArgumentType<String> = StringArgumentType.string()

    override fun useNativeSuggestions(): Boolean = false

    private val invalidUuid = SimpleCommandExceptionType(LiteralMessage("Failed to parse UUID"))

    override fun parse(reader: StringReader): UUID? {
        val input = reader.readString()
            ?.takeIf { it.isNotBlank() }
            ?: return null

        try {
            return UUID.fromString(input)
        } catch (e: IllegalArgumentException) {
            throw CommandSyntaxException(invalidUuid, LiteralMessage(e.message), input, 0)
        }
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> = Suggestions.empty()
}
