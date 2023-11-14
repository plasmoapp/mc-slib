package su.plo.slib.api.chat.converter

import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.component.McTranslatableText
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.language.ServerTranslator

abstract class ServerTextConverter<T> : TranslatableTextConverter<T>() {

    /**
     * Converts a [McTextComponent] to a JSON string while considering the server languages.
     *
     * @param source The source [McCommandSource] to determine the server's language.
     * @param text   The [McTextComponent] to be converted.
     * @return A JSON string representing the converted text component.
     */
    fun convertToJson(source: McCommandSource, text: McTextComponent): String {
        val language = ServerTranslator.getLanguage(source.language)

        val convertedText = translateInner(language, text)
        if (convertedText !is McTranslatableText)
            return convertToJson(text)

        if (!language.containsKey(convertedText.key))
            return convertToJson(text)

        return convertToJson(translate(
            language,
            convertedText
        ))
    }

    /**
     * Converts a [McTextComponent] to a server-specific implementation while considering the server languages.
     *
     * @param source The source [McCommandSource] to determine the server's language.
     * @param text   The [McTextComponent] to be converted.
     * @return A server-specific implementation of type [T] representing the converted text component.
     */
    fun convert(source: McCommandSource, text: McTextComponent): T {
        val language = ServerTranslator.getLanguage(source.language)

        val convertedText = translateInner(language, text)
        if (convertedText !is McTranslatableText)
            return convert(text)

        if (!language.containsKey(convertedText.key))
            return convert(text)

        return convert(translate(
            language,
            convertedText
        ))
    }
}
