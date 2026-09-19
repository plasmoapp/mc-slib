package su.plo.slib.api.command.brigadier

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import org.jetbrains.annotations.ApiStatus
import java.util.concurrent.CompletableFuture

/**
 * An argument type that wraps a native argument type.
 *
 * The native type is sent to the client for client-side completions and syntax validation,
 * while the server uses custom parsing logic to produce the parsed type.
 *
 * @param PARSED The custom type produced by server-side parsing
 * @param NATIVE The native type sent to the client
 */
interface CustomArgumentType<PARSED, NATIVE> : ArgumentType<PARSED> {
    /**
     * The native argument type sent to the client.
     *
     * Must be a plain brigadier primitive (`bool`, `double`, `float`, `integer`, `long`, `string`)
     * or a type from [su.plo.slib.api.server.command.brigadier.McArgumentTypes].
     */
    val nativeType: ArgumentType<NATIVE>

    /**
     * Whether native suggestions should be used.
     *
     * Set to `false` if you want to implement custom [listSuggestions].
     */
    fun useNativeSuggestions(): Boolean =
        true

    /**
     * Suggestions of the [nativeType].
     *
     * Override to implement custom suggestions, along with [useNativeSuggestions] set to `false`.
     */
    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> =
        nativeType.listSuggestions(context, builder)

    /**
     * This is controlled client-side and can't be changed server-side.
     */
    @ApiStatus.NonExtendable
    override fun getExamples(): Collection<String> =
        nativeType.examples
}
