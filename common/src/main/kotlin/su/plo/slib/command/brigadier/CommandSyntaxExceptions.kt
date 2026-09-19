package su.plo.slib.command.brigadier

import com.mojang.brigadier.Message
import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.style.McTextClickEvent
import su.plo.slib.api.chat.style.McTextStyle
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.command.brigadier.McTextMessage
import su.plo.slib.chat.MessageTextConverter
import kotlin.math.max
import kotlin.math.min

private val unknownCommandException = SimpleCommandExceptionType(
    McTextMessage.of(McTextComponent.translatable("command.unknown.command"))
)

private val unknownArgumentException = SimpleCommandExceptionType(
    McTextMessage.of(McTextComponent.translatable("command.unknown.argument"))
)

fun CommandSyntaxException.localizedFor(source: McBrigadierSource): CommandSyntaxException =
    localizedFor(source.source.language)

fun CommandSyntaxException.localizedFor(language: String): CommandSyntaxException {
    val message = rawMessage as? McTextMessage ?: return this
    val converter = MessageTextConverter.converterOrNull() ?: return this
    val converted = converter.convert(language, message.component)

    val parsedInput = input ?: return CommandSyntaxException(type, converted)

    return CommandSyntaxException(type, converted, parsedInput, cursor)
}

fun <S> ParseResults<S>.parseException(): CommandSyntaxException? {
    if (reader.canRead()) {
        exceptions.values.singleOrNull()?.let { return it }

        val type = if (context.range.isEmpty) unknownCommandException else unknownArgumentException

        return type.createWithContext(reader)
    }

    if (context.lastChild.command == null) {
        return unknownCommandException.createWithContext(reader)
    }

    return null
}

fun McCommandSource.sendFailure(message: McTextComponent) {
    sendMessage(McTextComponent.empty().append(message).withStyle(McTextStyle.RED))
}

fun McCommandSource.sendFailure(exception: CommandSyntaxException) {
    sendFailure(exception.rawMessage.toMcTextComponent())
}

fun McCommandSource.sendParseFailure(exception: CommandSyntaxException, command: String) {
    sendFailure(exception)

    val input = exception.input ?: return
    if (exception.cursor < 0) return

    val cursor = min(input.length, exception.cursor)
    val context = McTextComponent.empty()
        .withStyle(McTextStyle.GRAY)
        .clickEvent(McTextClickEvent.suggestCommand("/$command"))

    if (cursor > 10) {
        context.append(McTextComponent.literal("..."))
    }

    context.append(McTextComponent.literal(input.substring(max(0, cursor - 10), cursor)))

    if (cursor < input.length) {
        context.append(
            McTextComponent.literal(input.substring(cursor)).withStyle(McTextStyle.RED, McTextStyle.UNDERLINE)
        )
    }

    context.append(
        McTextComponent.translatable("command.context.here").withStyle(McTextStyle.RED, McTextStyle.ITALIC)
    )

    sendFailure(context)
}

private fun Message.toMcTextComponent(): McTextComponent =
    (this as? McTextMessage)?.component ?: McTextComponent.literal(string)
