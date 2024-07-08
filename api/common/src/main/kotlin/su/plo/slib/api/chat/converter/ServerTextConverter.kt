package su.plo.slib.api.chat.converter

import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.McCommandSource

interface ServerTextConverter<T> : McTextConverter<T> {

    /**
     * Converts a [McTextComponent] to a JSON string while considering the server languages.
     *
     * @param source The source [McCommandSource] to determine the server's language.
     * @param text   The [McTextComponent] to be converted.
     * @return A JSON string representing the converted text component.
     */
    fun convertToJson(source: McCommandSource, text: McTextComponent): String =
        convertToJson(source.language, text)

    /**
     * Converts a [McTextComponent] to a server-specific implementation while considering the server languages.
     *
     * @param source The source [McCommandSource] to determine the server's language.
     * @param text   The [McTextComponent] to be converted.
     * @return A server-specific implementation of type [T] representing the converted text component.
     */
    fun convert(source: McCommandSource, text: McTextComponent): T =
        convert(source.language, text)

    /**
     * Converts a [McTextComponent] to a server-specific implementation while considering the server languages.
     *
     * @param language The server's language.
     * @param text   The [McTextComponent] to be converted.
     * @return A server-specific implementation of type [T] representing the converted text component.
     */
    fun convertToJson(language: String, text: McTextComponent): String

    /**
     * Converts a [McTextComponent] to a server-specific implementation while considering the server languages.
     *
     * @param language The server's language.
     * @param text   The [McTextComponent] to be converted.
     * @return A server-specific implementation of type [T] representing the converted text component.
     */
    fun convert(language: String, text: McTextComponent): T
}
