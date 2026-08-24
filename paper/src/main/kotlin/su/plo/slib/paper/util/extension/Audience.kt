package su.plo.slib.paper.util.extension

import net.kyori.adventure.platform.bukkit.MinecraftComponentSerializer
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.command.CommandSender
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.paper.PaperServerLib

private fun nativeClass(vararg name: String): Class<*> =
    Class.forName(name.joinToString("."))

private val nativeComponentClass =
    nativeClass("net", "kyori", "adventure", "text", "Component")

private val nativeGsonSerializer =
    nativeClass("net", "kyori", "adventure", "text", "serializer", "gson", "GsonComponentSerializer")
        .getDeclaredMethod("gson")
        .invoke(null)

private val nativeDeserializeMethod =
    nativeClass("net", "kyori", "adventure", "text", "serializer", "ComponentSerializer")
        .getDeclaredMethod("deserialize", Any::class.java)

private val nativeAudienceClass =
    nativeClass("net", "kyori", "adventure", "audience", "Audience")

private val nativeSendMessageMethod =
    nativeAudienceClass.getDeclaredMethod("sendMessage", nativeComponentClass)

private val nativeSendActionBarMethod =
    nativeAudienceClass.getDeclaredMethod("sendActionBar", nativeComponentClass)

fun CommandSender.sendMessage(minecraftServer: PaperServerLib, text: McTextComponent) {
    val json = minecraftServer.textConverter.convertToJson(text)

    if (MinecraftComponentSerializer.isSupported()) {
        minecraftServer.adventure.sender(this)
            .sendMessage(GsonComponentSerializer.gson().deserialize(json))
        return
    }

    nativeSendMessageMethod.invoke(this, nativeDeserializeMethod.invoke(nativeGsonSerializer, json))
}

fun CommandSender.sendActionBar(minecraftServer: PaperServerLib, text: McTextComponent) {
    val json = minecraftServer.textConverter.convertToJson(text)

    if (MinecraftComponentSerializer.isSupported()) {
        minecraftServer.adventure.sender(this)
            .sendActionBar(GsonComponentSerializer.gson().deserialize(json))
        return
    }

    nativeSendActionBarMethod.invoke(this, nativeDeserializeMethod.invoke(nativeGsonSerializer, json))
}
