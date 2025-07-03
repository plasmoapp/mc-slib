package su.plo.slib.spigot.util.extension

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.command.CommandSender
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.spigot.SpigotServerLib

private fun findClassOrNull(name: String): Class<*>? =
    try {
        Class.forName(name)
    } catch (_: ClassNotFoundException) {
        null
    }

private fun findClassOrNull(name: List<String>): Class<*>? =
    findClassOrNull(name.joinToString("."))

private val nativeComponentClass = findClassOrNull(
    listOf("net", "kyori", "adventure", "text", "Component")
)
private val nativeGsonSerializerClass = findClassOrNull(
    listOf("net", "kyori", "adventure", "text", "serializer", "gson", "GsonComponentSerializer")
)
private val nativeComponentSerializerClass = findClassOrNull(
    listOf("net", "kyori", "adventure", "text", "serializer", "ComponentSerializer")
)
private val nativeGsonGetterMethod = run {
    if (nativeGsonSerializerClass == null) return@run null

    try {
        nativeGsonSerializerClass.getDeclaredMethod("gson")
    } catch (_: NoSuchMethodException) {
        null
    }
}
private val nativeComponentDeserializeMethod = run {
    if (nativeComponentSerializerClass == null) return@run null

    try {
        nativeComponentSerializerClass.getDeclaredMethod("deserialize", Any::class.java)
    } catch (_: NoSuchMethodException) {
        null
    }
}

private val nativeAudienceClass = findClassOrNull(
    listOf("net", "kyori", "adventure", "audience", "Audience")
)
private val nativeAudienceSendMessageMethod = run {
    if (nativeAudienceClass == null || nativeComponentClass == null) return@run null

    try {
        nativeAudienceClass.getDeclaredMethod("sendMessage", nativeComponentClass)
    } catch (_: NoSuchMethodException) {
        null
    }
}
private val nativeAudienceSendActionBarMethod = run {
    if (nativeAudienceClass == null || nativeComponentClass == null) return@run null

    try {
        nativeAudienceClass.getDeclaredMethod("sendActionBar", nativeComponentClass)
    } catch (_: NoSuchMethodException) {
        null
    }
}

fun CommandSender.sendMessage(minecraftServer: SpigotServerLib, text: McTextComponent) {
    val json = minecraftServer.textConverter.convertToJson(text)
    if (
        nativeAudienceSendMessageMethod == null ||
        nativeComponentDeserializeMethod == null ||
        nativeGsonGetterMethod == null ||
        nativeAudienceClass == null ||
        !nativeAudienceClass.isInstance(this)
    ) {
        val audience = minecraftServer.adventure.sender(this)
        val component = GsonComponentSerializer.gson().deserialize(json)
        audience.sendMessage(component)
        return
    }

    val gson = nativeGsonGetterMethod.invoke(null)
    val component = nativeComponentDeserializeMethod.invoke(gson, json)
    nativeAudienceSendMessageMethod.invoke(this, component)
}


fun CommandSender.sendActionBar(minecraftServer: SpigotServerLib, text: McTextComponent) {
    val json = minecraftServer.textConverter.convertToJson(text)
    if (
        nativeAudienceSendActionBarMethod == null ||
        nativeComponentDeserializeMethod == null ||
        nativeGsonGetterMethod == null ||
        nativeAudienceClass == null ||
        !nativeAudienceClass.isInstance(this)
    ) {
        val audience = minecraftServer.adventure.sender(this)
        val component = GsonComponentSerializer.gson().deserialize(json)
        audience.sendActionBar(component)
        return
    }

    val gson = nativeGsonGetterMethod.invoke(null)
    val component = nativeComponentDeserializeMethod.invoke(gson, json)
    nativeAudienceSendActionBarMethod.invoke(this, component)
}
