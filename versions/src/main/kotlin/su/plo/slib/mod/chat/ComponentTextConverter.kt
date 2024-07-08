package su.plo.slib.mod.chat

import net.minecraft.network.chat.Component
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.converter.McTextConverter
import su.plo.slib.chat.AdventureComponentTextConverter

//#if MC>=12005
//$$ import com.google.gson.*
//$$ import com.mojang.serialization.JsonOps
//$$ import net.minecraft.network.chat.ComponentSerialization
//#endif

object ComponentTextConverter : McTextConverter<Component> {

    //#if MC>=12005
    //$$ private val gson: Gson = GsonBuilder().disableHtmlEscaping().create()
    //#endif

    private val textConverter = AdventureComponentTextConverter()

    override fun convert(text: McTextComponent): Component =
        convertFromJson(textConverter.convertToJson(text))

    override fun convertFromJson(json: String): Component =
    //#if MC>=12005
    //$$ JsonParser.parseString(json)
    //$$     ?.let {
    //$$         ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, it).getOrThrow(::JsonParseException)
    //$$     }
    //$$     ?: throw JsonParseException("JsonParser return null smh")
        //#else
        Component.Serializer.fromJson(json)!!
    //#endif

    override fun convertToJson(text: Component): String =
    //#if MC>=12005
    //$$ gson.toJson(
    //$$     ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow(::JsonParseException)
    //$$ )
        //#else
        Component.Serializer.toJson(text)
    //#endif
}