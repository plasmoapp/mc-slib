package su.plo.slib.command.brigadier

import com.mojang.brigadier.Command
import com.mojang.brigadier.RedirectModifier
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContextBuilder
import com.mojang.brigadier.context.ParsedArgument
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.CommandNode
import su.plo.slib.api.command.brigadier.CustomArgumentType
import su.plo.slib.api.command.brigadier.McBrigadierSource
import java.util.function.Predicate

@Suppress("UNCHECKED_CAST")
class CustomArgumentCommandNode<S, PARSED, NATIVE>(
    name: String,
    val customArgumentType: CustomArgumentType<PARSED, NATIVE>,
    private val sourceFactory: (S) -> McBrigadierSource,
    command: Command<S>?,
    requirement: Predicate<S>,
    redirect: CommandNode<S>?,
    modifier: RedirectModifier<S>?,
    forks: Boolean,
    customSuggestions: SuggestionProvider<S>?,
) : ArgumentCommandNode<S, NATIVE>(
    name,
    customArgumentType.unwrapNativeType() as ArgumentType<NATIVE>,
    command,
    requirement,
    redirect,
    modifier,
    forks,
    customSuggestions,
) {
    override fun parse(reader: StringReader, contextBuilder: CommandContextBuilder<S>) {
        val start = reader.cursor
        val result = (
            try {
                customArgumentType.parse(reader)
            } catch (e: CommandSyntaxException) {
                throw e.localizedFor(sourceFactory(contextBuilder.source))
            }
        )
            ?: error("CustomArgumentType ${customArgumentType::class.java.name} returned null from parse; throw a CommandSyntaxException to signal a parse failure")

        val parsed = ParsedArgument<S, PARSED>(start, reader.cursor, result)

        contextBuilder.withArgument(name, parsed)
        contextBuilder.withNode(this, parsed.range)
    }
}

fun <S, PARSED, NATIVE> RequiredArgumentBuilder<S, PARSED>.buildCustom(
    sourceFactory: (S) -> McBrigadierSource,
): CustomArgumentCommandNode<S, PARSED, NATIVE> {
    @Suppress("UNCHECKED_CAST")
    val type = type as CustomArgumentType<PARSED, NATIVE>

    val result = CustomArgumentCommandNode<S, PARSED, NATIVE>(
        name,
        type,
        sourceFactory,
        command,
        requirement,
        redirect,
        redirectModifier,
        isFork,
        suggestionsProvider ?:
            if (!type.useNativeSuggestions()) {
                SuggestionProvider<S> { context, builder -> type.listSuggestions(context, builder) }
            } else {
                null
            },
    )

    arguments.forEach { result.addChild(it) }

    return result
}

tailrec fun ArgumentType<*>.unwrapNativeType(): ArgumentType<*> =
    if (this is CustomArgumentType<*, *>) nativeType.unwrapNativeType() else this
