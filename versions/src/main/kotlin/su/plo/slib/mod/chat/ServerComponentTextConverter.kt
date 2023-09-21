package su.plo.slib.mod.chat

import net.minecraft.network.chat.Component
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.converter.ServerTextConverter
import su.plo.slib.api.language.ServerLanguages

class ServerComponentTextConverter(
    languages: ServerLanguages
) : ServerTextConverter<Component>(languages) {

    private val textConverter = ComponentTextConverter()

    override fun convertToJson(text: Component) =
        textConverter.convertToJson(text)

    override fun convertFromJson(json: String) =
        textConverter.convertFromJson(json)

    override fun convert(text: McTextComponent) =
        textConverter.convert(text)
}
