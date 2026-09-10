package su.plo.slib.chat

import com.mojang.brigadier.Message
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.service.lazyServiceOrNull

interface MessageTextConverter {
    fun convert(language: String, text: McTextComponent): Message

    companion object {
        private val provider: MessageTextConverter? by lazyServiceOrNull()

        fun converterOrNull(): MessageTextConverter? =
            provider
    }
}
