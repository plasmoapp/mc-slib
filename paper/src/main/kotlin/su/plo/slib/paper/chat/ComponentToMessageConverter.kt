@file:Suppress("UnstableApiUsage")

package su.plo.slib.paper.chat

import com.mojang.brigadier.Message
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.chat.AdventureComponentTextConverter
import su.plo.slib.chat.MessageTextConverter

class ComponentToMessageConverter : MessageTextConverter {
    private val textConverter = AdventureComponentTextConverter()
    private val gson = GsonComponentSerializer.gson()

    override fun convert(language: String, text: McTextComponent): Message {
        val json = textConverter.convertToJson(language, text)

        return MessageComponentSerializer.message().serialize(gson.deserialize(json))
    }
}
