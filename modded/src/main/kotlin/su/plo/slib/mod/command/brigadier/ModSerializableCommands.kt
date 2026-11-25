package su.plo.slib.mod.command.brigadier

//? if >=26.4 {
/*import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.permission.v1.PermissionContextOwner
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.ServerPlayerSuggestionContext
import net.minecraft.commands.arguments.BooleanArgument
import net.minecraft.commands.arguments.DoubleArgument
import net.minecraft.commands.arguments.FloatArgument
import net.minecraft.commands.arguments.IntegerArgument
import net.minecraft.commands.arguments.LongArgument
import net.minecraft.commands.arguments.SerializableArgument
import net.minecraft.commands.arguments.StringArgument
import net.minecraft.commands.suggestions.ServerOnlyCustomSuggestion
import net.minecraft.commands.suggestions.SuggestionContext
import net.minecraft.server.level.ServerPlayer
import su.plo.slib.api.command.brigadier.CustomArgumentType
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.command.brigadier.copyFor
import su.plo.slib.command.brigadier.localizedFor
import su.plo.slib.command.brigadier.unwrapNativeType
import su.plo.slib.mod.ModServerLib
import su.plo.slib.mod.permission.sourcePlayer
import java.util.concurrent.CompletableFuture

fun SuggestionProvider<CommandSourceStack>.toServerSuggestion(): SuggestionProvider<CommandSourceStack> =
    ServerOnlyCustomSuggestion { _, context, builder ->
        val player = context.source.entity() as? ServerPlayer
            ?: return@ServerOnlyCustomSuggestion builder.buildFuture()

        getSuggestions(context.copyFor(player.createCommandSourceStack()), builder)
    }.toBrigadier()

fun <S> ArgumentType<*>.listNativeSuggestions(
    context: CommandContext<S>,
    builder: SuggestionsBuilder,
): CompletableFuture<Suggestions> {
    val suggestionContext = context.source.toSuggestionContext() ?: return builder.buildFuture()

    return listSuggestions(context.copyFor(suggestionContext), builder)
}

private fun Any?.toSuggestionContext(): SuggestionContext? =
    when (this) {
        is SuggestionContext -> this
        is McBrigadierSource -> getInstance<Any?>().toSuggestionContext()
        is CommandSourceStack -> player?.let(::ServerPlayerSuggestionContext)
        else -> null
    }

fun ArgumentType<*>.toSerializableArgumentOrThrow(): ArgumentType<*>? {
    if (this is SerializableArgument.BrigadierWrapper<*>) return null

    return toSerializableArgument()
        ?: throw IllegalArgumentException("argument type $this isn't supported since 26.4")
}

@Suppress("UNCHECKED_CAST")
fun ArgumentType<*>.toSerializableArgument(): ArgumentType<*>? =
    when (this) {
        is SerializableArgument.BrigadierWrapper<*> -> null
        is CustomArgumentType<*, *> -> CustomArgumentWrapper(this as CustomArgumentType<Any, *>)
        is StringArgumentType -> when (type) {
            StringArgumentType.StringType.SINGLE_WORD -> StringArgument.word()
            StringArgumentType.StringType.QUOTABLE_PHRASE -> StringArgument.string()
            StringArgumentType.StringType.GREEDY_PHRASE -> StringArgument.greedyString()
            null -> null
        }
        is IntegerArgumentType -> IntegerArgument.integer(minimum, maximum)
        is LongArgumentType -> LongArgument.longArg(minimum, maximum)
        is FloatArgumentType -> FloatArgument.floatArg(minimum, maximum)
        is DoubleArgumentType -> DoubleArgument.doubleArg(minimum, maximum)
        is BoolArgumentType -> BooleanArgument.bool()
        else -> null
    }

@Suppress("UNCHECKED_CAST")
private class CustomArgumentWrapper<PARSED : Any>(
    private val customType: CustomArgumentType<PARSED, *>,
) : SerializableArgument.BrigadierWrapper<PARSED> {
    private val nativeType: SerializableArgument.BrigadierWrapper<*> =
        customType.unwrapNativeType().let { it.toSerializableArgument() ?: it }
            as? SerializableArgument.BrigadierWrapper<*>
            ?: throw IllegalArgumentException(
                "native type of ${customType::class.java.name} isn't supported by the client: ${customType.unwrapNativeType()}"
            )

    override fun argument(): SerializableArgument<PARSED> =
        nativeType.argument() as SerializableArgument<PARSED>

    override fun parse(reader: StringReader): PARSED =
        parseNotNull(reader)

    override fun <S : Any> parse(reader: StringReader, source: S): PARSED =
        try {
            parseNotNull(reader)
        } catch (e: CommandSyntaxException) {
            throw e.localizedFor(source.parseLanguage())
        }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> =
        nativeType.listSuggestions(context, builder)

    private fun parseNotNull(reader: StringReader): PARSED =
        customType.parse(reader)
            ?: error("CustomArgumentType ${customType::class.java.name} returned null from parse; throw a CommandSyntaxException to signal a parse failure")
}

private fun Any.parseLanguage(): String {
    val player = (this as? PermissionContextOwner)?.permissionContext?.sourcePlayer()
        ?.takeIf { ModServerLib.isBound }
        ?: return "en_us"

    return ModServerLib.getPlayerById(player.uuid)?.language ?: "en_us"
}
*///?}
