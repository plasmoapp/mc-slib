package su.plo.slib.mod.chat

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.converter.McTextConverter
import su.plo.slib.chat.AdventureComponentTextConverter

object ComponentTextConverter : McTextConverter<Component> {

    private val gson: Gson = GsonBuilder().disableHtmlEscaping().create()

    private val textConverter = AdventureComponentTextConverter()

    override fun convert(text: McTextComponent): Component =
        convertFromJson(textConverter.convertToJson(text))

    override fun convertFromJson(json: String): Component =
        JsonParser.parseString(json)
            ?.let {
                ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, it).getOrThrow(::JsonParseException)
            }
            ?: throw JsonParseException("JsonParser returned null")

    override fun convertToJson(text: Component): String =
        gson.toJson(
            ComponentSerialization.CODEC
                .encodeStart(JsonOps.INSTANCE, text)
                .getOrThrow(::JsonParseException)
        )
}
