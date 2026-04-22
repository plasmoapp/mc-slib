package su.plo.slib.minestom.chat

import com.mojang.brigadier.Message
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.converter.MessageTextConverter

class ComponentToMessageConverter : MessageTextConverter {
    override fun convert(text: McTextComponent): Message =
        McTextMessage(text)
}
