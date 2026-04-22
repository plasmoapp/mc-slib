package su.plo.slib.velocity.chat

import com.mojang.brigadier.Message
import com.velocitypowered.api.command.VelocityBrigadierMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.converter.MessageTextConverter
import su.plo.slib.chat.AdventureComponentTextConverter

class ComponentToMessageConverter : MessageTextConverter {
    private val textConverter = AdventureComponentTextConverter()
    private val gson = GsonComponentSerializer.gson()

    override fun convert(text: McTextComponent): Message {
        val json = textConverter.convertToJson(text)
        return VelocityBrigadierMessage.tooltip(gson.deserialize(json))
    }
}
