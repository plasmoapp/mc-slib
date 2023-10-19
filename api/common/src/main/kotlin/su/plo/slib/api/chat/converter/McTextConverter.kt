package su.plo.slib.api.chat.converter

import su.plo.slib.api.chat.component.McTextComponent
import java.util.stream.Collectors

/**
 * The [McTextConverter] interface defines the contract for converting Minecraft text components
 * represented by [McTextComponent] into server-specific implementations of type [T].
 *
 * @param T The type representing the converted server-specific implementation.
 */
interface McTextConverter<T> {

    /**
     * Converts a single [McTextComponent] into a server-specific implementation of type [T].
     *
     * @param text The [McTextComponent] to be converted.
     * @return A server-specific implementation of type [T] representing the converted text component.
     */
    fun convert(text: McTextComponent): T

    /**
     * Converts a list of [McTextComponent] objects into a list of server-specific implementations of type [T].
     *
     * @param list The list of [McTextComponent] objects to be converted.
     * @return A list of server-specific implementations of type [T] representing the converted text components.
     */
    fun convert(list: List<McTextComponent>): List<T>? {
        return list.map { text -> this.convert(text) }
    }

    /**
     * Converts a value of type [T] to a JSON string.
     *
     * @param text The value of type [T] to be converted.
     * @return A JSON string representing the converted text.
     */
    fun convertToJson(text: T): String

    /**
     * Converts a [McTextComponent] to a JSON string.
     *
     * @param text The [McTextComponent] to be converted.
     * @return A JSON string representing the converted text component.
     */
    fun convertToJson(text: McTextComponent): String {
        return convertToJson(convert(text))
    }

    /**
     * Converts a JSON string to a value of type [T].
     *
     * @param json The JSON string to be converted.
     * @return A value of type [T] representing the converted JSON.
     */
    fun convertFromJson(json: String): T
}

