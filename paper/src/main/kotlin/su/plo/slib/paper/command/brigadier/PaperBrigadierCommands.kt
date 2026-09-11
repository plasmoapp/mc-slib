@file:Suppress("UnstableApiUsage")

package su.plo.slib.paper.command.brigadier

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
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
import su.plo.slib.command.brigadier.localizedFor
import su.plo.slib.command.brigadier.proxied
import su.plo.slib.command.brigadier.unwrapNativeType
import su.plo.slib.paper.PaperServerLib
import su.plo.slib.paper.command.PaperUnboundCommandSource
import java.lang.ref.WeakReference
import java.util.concurrent.CompletableFuture

// Paper only uses ArgumentType#parse(reader, source) since 1.21.8, so older versions can't get the source
// so we're storing it in thread local at source factory (it'll always happen before possible throw)
// and reusing it at try/catch place
// if source still can't be resolved, so default language is used instead
private val parsingSourceStack = ThreadLocal<WeakReference<CommandSourceStack>>()

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
                    { sourceStack ->
                        parsingSourceStack.set(WeakReference(sourceStack))
                        PaperBrigadierSource.from(sourceStack)
                    },
                    { it.toMc() },
                    { it.toPaperArgumentType() },
                ),
                description,
                aliases,
            )
        }
}

private fun parseLanguage(source: Any?): String {
    val sourceStack = source as? CommandSourceStack ?: parsingSourceStack.get()?.get()

    return sourceStack?.let { PaperBrigadierSource.from(it).source.language }
        ?: PaperServerLib.instanceOrNull?.serverTranslator?.defaultLanguage
        ?: PaperUnboundCommandSource.language
}

@Suppress("UNCHECKED_CAST")
internal fun ArgumentType<*>.toPaperArgumentType(): ArgumentType<*>? =
    if (this is CustomArgumentType<*, *>) PaperArgumentTypeAdapter(this as CustomArgumentType<Any, Any>)
    else null

internal class PaperArgumentTypeAdapter<PARSED : Any, NATIVE : Any>(
    private val delegate: CustomArgumentType<PARSED, NATIVE>,
) : PaperCustomArgumentType<PARSED, NATIVE> {
    @Suppress("UNCHECKED_CAST")
    private val nativeType = delegate.nativeType.unwrapNativeType() as ArgumentType<NATIVE>

    override fun getNativeType(): ArgumentType<NATIVE> =
        nativeType

    override fun parse(reader: StringReader): PARSED =
        parseLocalized(reader, null)

    override fun <S : Any> parse(reader: StringReader, source: S): PARSED =
        parseLocalized(reader, source)

    private fun parseLocalized(reader: StringReader, source: Any?): PARSED =
        try {
            delegate.parse(reader)
        } catch (e: CommandSyntaxException) {
            throw e.localizedFor(parseLanguage(source))
        }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> =
        if (delegate.useNativeSuggestions()) nativeType.listSuggestions(context, builder)
        else delegate.listSuggestions(context, builder)
}

fun CommandContext<CommandSourceStack>.toMc(): CommandContext<McBrigadierSource> =
    copyFor(PaperBrigadierSource.from(source))
