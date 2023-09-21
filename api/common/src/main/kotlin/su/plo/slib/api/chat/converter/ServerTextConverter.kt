package su.plo.slib.api.chat.converter

import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.component.McTranslatableText
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.language.ServerLanguages

abstract class ServerTextConverter<T>(
    private val languages: ServerLanguages
) : TranslatableTextConverter<T>() {

    fun convertToJson(source: McCommandSource, text: McTextComponent): String {
        val language = languages.getServerLanguage(source)

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

    fun convert(source: McCommandSource, text: McTextComponent): T {
        val language = languages.getServerLanguage(source)

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
