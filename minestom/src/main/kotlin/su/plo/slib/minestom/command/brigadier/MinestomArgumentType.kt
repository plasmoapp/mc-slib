package su.plo.slib.minestom.command.brigadier

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.exception.ArgumentSyntaxException

private val parsingSender = ThreadLocal<CommandSender>()

internal fun <T> withParsingSender(sender: CommandSender, block: () -> T): T {
    val previous = parsingSender.get()
    parsingSender.set(sender)

    try {
        return block()
    } finally {
        if (previous == null) parsingSender.remove() else parsingSender.set(previous)
    }
}

data class MinestomArgumentType<S>(
    val argumentBuilder: (String) -> Argument<S>,
) : ArgumentType<S> {
    private val parsingArgument by lazy { argumentBuilder("slib_parsing") }

    override fun parse(reader: StringReader): S {
        val sender = parsingSender.get()
            ?: throw UnsupportedOperationException(
                "${this::class.java.name} can only be parsed while a command is being parsed"
            )

        val input = reader.remaining

        val parsed =
            try {
                parsingArgument.parse(sender, input)
            } catch (e: ArgumentSyntaxException) {
                throw SimpleCommandExceptionType(LiteralMessage(e.message))
                    .createWithContext(reader)
            }

        reader.cursor = reader.totalLength

        return parsed
    }
}
