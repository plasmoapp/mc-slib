package su.plo.slib.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

fun Component.toJson(): String =
    GsonComponentSerializer.gson().serialize(this)

@Suppress("UNCHECKED_CAST")
fun<T : Component> fromJson(json: String): T =
    GsonComponentSerializer.gson().deserialize(json) as T
