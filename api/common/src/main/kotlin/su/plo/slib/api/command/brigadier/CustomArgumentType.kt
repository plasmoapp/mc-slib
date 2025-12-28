package su.plo.slib.api.command.brigadier

import com.mojang.brigadier.arguments.ArgumentType
import org.jetbrains.annotations.ApiStatus

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
     */
    val nativeType: ArgumentType<NATIVE>

    /**
     * Whether native suggestions should be used.
     *
     * Set to `false` is you want to implement custom [listSuggestions].
     */
    fun useNativeSuggestions(): Boolean =
        true

    /**
     * This is controlled client-side and can't be changed server-side.
     */
    @ApiStatus.NonExtendable
    override fun getExamples(): Collection<String> =
        nativeType.examples
}
