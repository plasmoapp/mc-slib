package su.plo.slib.mod.chat

import net.minecraft.network.chat.Component
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.converter.McTextConverter
import su.plo.slib.chat.AdventureComponentTextConverter

//? if >=1.20.5 {
/*import com.google.gson.*
import com.mojang.serialization.JsonOps
import net.minecraft.network.chat.ComponentSerialization
*///?}

object ComponentTextConverter : McTextConverter<Component> {

    //? if >=1.20.5 {
    /*private val gson: Gson = GsonBuilder().disableHtmlEscaping().create()
    *///?}

    private val textConverter = AdventureComponentTextConverter()

    override fun convert(text: McTextComponent): Component =
        convertFromJson(textConverter.convertToJson(text))

    override fun convertFromJson(json: String): Component =
    //? if >=1.20.5 {
    /*JsonParser.parseString(json)
        ?.let {
            ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, it).getOrThrow(::JsonParseException)
        }
        ?: throw JsonParseException("JsonParser return null smh")
        *///?} else {
        Component.Serializer.fromJson(json)!!
    //?}

    override fun convertToJson(text: Component): String =
    //? if >=1.20.5 {
    /*gson.toJson(
        ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow(::JsonParseException)
    )
        *///?} else {
        Component.Serializer.toJson(text)
    //?}
}