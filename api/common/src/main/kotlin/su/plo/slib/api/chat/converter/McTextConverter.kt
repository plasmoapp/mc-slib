package su.plo.slib.api.chat.converter

import su.plo.slib.api.chat.component.McTextComponent
import java.util.stream.Collectors

interface McTextConverter<T> {

    fun convertToJson(text: T): String

    fun convertToJson(text: McTextComponent): String {
        return convertToJson(convert(text))
    }

    fun convertFromJson(json: String): T

    fun convert(text: McTextComponent): T

    fun convert(list: List<McTextComponent>): List<T>? {
        return list.stream()
            .map { text: McTextComponent -> this.convert(text) }
            .collect(Collectors.toList())
    }
}
