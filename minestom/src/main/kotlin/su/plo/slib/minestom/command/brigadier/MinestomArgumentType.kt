package su.plo.slib.minestom.command.brigadier

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import net.minestom.server.command.builder.arguments.Argument

data class MinestomArgumentType<S>(
    val argumentBuilder: (String) -> Argument<S>,
) : ArgumentType<S> {
    override fun parse(reader: StringReader): S {
        throw UnsupportedOperationException()
    }
}
