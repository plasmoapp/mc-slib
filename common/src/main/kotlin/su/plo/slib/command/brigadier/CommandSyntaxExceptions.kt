package su.plo.slib.command.brigadier

import com.mojang.brigadier.exceptions.CommandSyntaxException
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.style.McTextStyle
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.command.brigadier.McTextMessage
import su.plo.slib.chat.MessageTextConverter

fun CommandSyntaxException.localizedFor(source: McBrigadierSource): CommandSyntaxException {
    val message = rawMessage as? McTextMessage ?: return this
    val converter = MessageTextConverter.converterOrNull() ?: return this
    val converted = converter.convert(source.source.language, message.component)

    val parsedInput = input ?: return CommandSyntaxException(type, converted)

    return CommandSyntaxException(type, converted, parsedInput, cursor)
}

fun CommandSyntaxException.toMcTextComponent(): McTextComponent {
    val rawMessage = rawMessage
    val message =
        if (rawMessage is McTextMessage) rawMessage.component
        else McTextComponent.literal(rawMessage.string)

    val context = context ?: return message.withStyle(McTextStyle.RED)

    return McTextComponent.translatable(
        "command.context.parse_error",
        message,
        McTextComponent.literal(cursor.toString()),
        McTextComponent.literal(context),
    ).withStyle(McTextStyle.RED)
}
