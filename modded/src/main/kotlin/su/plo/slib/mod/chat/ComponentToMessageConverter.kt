package su.plo.slib.mod.chat

import com.mojang.brigadier.Message
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.chat.AdventureComponentTextConverter
import su.plo.slib.chat.MessageTextConverter

class ComponentToMessageConverter : MessageTextConverter {
    private val textConverter = AdventureComponentTextConverter()

    override fun convert(language: String, text: McTextComponent): Message =
        ComponentTextConverter.convertFromJson(textConverter.convertToJson(language, text))
}
