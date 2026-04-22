package su.plo.slib.spigot.chat

import com.mojang.brigadier.Message
import net.kyori.adventure.platform.bukkit.MinecraftComponentSerializer
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.converter.MessageTextConverter
import su.plo.slib.chat.AdventureComponentTextConverter

class ComponentToMessageConverter : MessageTextConverter {
    private val textConverter = AdventureComponentTextConverter()
    private val gson = GsonComponentSerializer.gson()

    override fun convert(text: McTextComponent): Message {
        val json = textConverter.convertToJson(text)
        val component = gson.deserialize(json)
        return MinecraftComponentSerializer.get().serialize(component) as Message
    }
}
