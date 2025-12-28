package su.plo.slib.command.brigadier

import com.mojang.brigadier.Command
import com.mojang.brigadier.RedirectModifier
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.CommandContextBuilder
import com.mojang.brigadier.context.ParsedArgument
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.CommandNode
import su.plo.slib.api.command.brigadier.CustomArgumentType
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate

class CustomArgumentCommandNode<S, PARSED, NATIVE>(
    name: String,
    val customArgumentType: CustomArgumentType<PARSED, NATIVE>,
    command: Command<S>,
    requirement: Predicate<S>,
    redirect: CommandNode<S>?,
    modifier: RedirectModifier<S>?,
    forks: Boolean,
    customSuggestions: SuggestionProvider<S>?,
) : ArgumentCommandNode<S, NATIVE>(
    name,
    customArgumentType.nativeType,
    command,
    requirement,
    redirect,
    modifier,
    forks,
    customSuggestions,
) {
    override fun parse(reader: StringReader, contextBuilder: CommandContextBuilder<S>) {
        val start = reader.cursor
        val result = customArgumentType.parse(reader)

        val parsed = ParsedArgument<S, PARSED>(start, reader.cursor, result)

        contextBuilder.withArgument(name, parsed)
        contextBuilder.withNode(this, parsed.range)
    }
}

fun <S, PARSED, NATIVE> RequiredArgumentBuilder<S, PARSED>.buildCustom(): CustomArgumentCommandNode<S, PARSED, NATIVE> {
    @Suppress("UNCHECKED_CAST")
    val type = type as CustomArgumentType<PARSED, NATIVE>

    val result = CustomArgumentCommandNode<S, PARSED, NATIVE>(
        name,
        type,
        command,
        requirement,
        redirect,
        redirectModifier,
        isFork,
        suggestionsProvider ?:
            if (!type.useNativeSuggestions()) {
                object : SuggestionProvider<S> {
                    override fun getSuggestions(
                        context: CommandContext<S?>,
                        builder: SuggestionsBuilder,
                    ): CompletableFuture<Suggestions> =
                        type.listSuggestions(context, builder)
                }
            } else {
                null
            },
    )

    arguments.forEach { result.addChild(it) }

    return result
}
