package su.plo.slib.server.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import su.plo.slib.api.command.brigadier.CustomArgumentType
import su.plo.slib.api.server.command.brigadier.McArgumentTypes
import su.plo.slib.api.server.command.brigadier.McGameProfilesArgumentResolver
import java.util.concurrent.CompletableFuture

private const val EVERYONE = "everyone"

sealed interface NestedGameProfilesTarget {
    data object Everyone : NestedGameProfilesTarget

    data class Selector(val resolver: McGameProfilesArgumentResolver) : NestedGameProfilesTarget
}

class NestedGameProfilesArgumentType :
    CustomArgumentType<NestedGameProfilesTarget, McGameProfilesArgumentResolver> {
    override val nativeType: ArgumentType<McGameProfilesArgumentResolver> =
        McArgumentTypes.gameProfiles()

    override fun useNativeSuggestions(): Boolean = false

    override fun parse(reader: StringReader): NestedGameProfilesTarget {
        val start = reader.cursor

        if (reader.readUnquotedString() == EVERYONE) {
            return NestedGameProfilesTarget.Everyone
        }

        reader.cursor = start

        return NestedGameProfilesTarget.Selector(nativeType.parse(reader))
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        if (!EVERYONE.startsWith(builder.remaining, ignoreCase = true)) {
            return super.listSuggestions(context, builder)
        }

        builder.suggest(EVERYONE)

        return builder.buildFuture()
    }
}
