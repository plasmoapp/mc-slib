@file:Suppress("UnstableApiUsage")

package su.plo.slib.paper.command.brigadier

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.CustomArgumentType as PaperCustomArgumentType
import su.plo.slib.api.command.brigadier.CustomArgumentType
import su.plo.slib.api.command.brigadier.McBrigadierRegistry
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.logging.McLogger
import su.plo.slib.command.brigadier.applyEach
import su.plo.slib.command.brigadier.collectBrigadierCommands
import su.plo.slib.command.brigadier.copyFor
import su.plo.slib.command.brigadier.proxied
import su.plo.slib.paper.PaperServerLib
import java.util.concurrent.CompletableFuture

internal fun Commands.collectAndApply(
    logger: McLogger,
    logRegistered: Boolean,
) {
    val phase =
        if (PaperServerLib.instanceOrNull == null) McBrigadierRegistry.Phase.BOOTSTRAP
        else McBrigadierRegistry.Phase.RUNTIME

    collectBrigadierCommands(phase)
        .applyEach(logger, logRegistered) { (node, description, aliases) ->
            register(
                node.proxied(
                    logger,
                    PaperBrigadierSource::from,
                    { it.toMc() },
                    { it.toPaperArgumentType() },
                ),
                description,
                aliases,
            )
        }
}

@Suppress("UNCHECKED_CAST")
internal fun ArgumentType<*>.toPaperArgumentType(): ArgumentType<*>? =
    if (this is CustomArgumentType<*, *>) PaperArgumentTypeAdapter(this as CustomArgumentType<Any, Any>)
    else null

private tailrec fun ArgumentType<*>.unwrapNativeType(): ArgumentType<*> =
    if (this is CustomArgumentType<*, *>) nativeType.unwrapNativeType() else this

internal class PaperArgumentTypeAdapter<PARSED : Any, NATIVE : Any>(
    private val delegate: CustomArgumentType<PARSED, NATIVE>,
) : PaperCustomArgumentType<PARSED, NATIVE> {
    @Suppress("UNCHECKED_CAST")
    private val nativeType = delegate.nativeType.unwrapNativeType() as ArgumentType<NATIVE>

    override fun getNativeType(): ArgumentType<NATIVE> =
        nativeType

    override fun parse(reader: StringReader): PARSED =
        delegate.parse(reader)

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> =
        if (delegate.useNativeSuggestions()) nativeType.listSuggestions(context, builder)
        else delegate.listSuggestions(context, builder)
}

fun CommandContext<CommandSourceStack>.toMc(): CommandContext<McBrigadierSource> =
    copyFor(PaperBrigadierSource.from(source))
