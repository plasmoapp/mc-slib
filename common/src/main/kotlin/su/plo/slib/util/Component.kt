package su.plo.slib.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

inline fun Component.toJson(): String =
    GsonComponentSerializer.gson().serialize(this)

inline fun<T : Component> fromJson(json: String): T =
    GsonComponentSerializer.gson().deserialize(json) as T