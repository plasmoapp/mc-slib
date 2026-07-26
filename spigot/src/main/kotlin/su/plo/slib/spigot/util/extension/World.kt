package su.plo.slib.spigot.util.extension

import org.bukkit.Bukkit
import org.bukkit.World

private val getKeyMethod by lazy {
    try {
        World::class.java.getMethod("getKey")
    } catch (_: NoSuchMethodException) {
        null
    }
}

fun World.getWorldKey(): String =
    getKeyMethod?.let { runCatching { it.invoke(this).toString() }.getOrNull() }
        ?: legacyKey()

private fun World.legacyKey(): String {
    val mainWorldName = Bukkit.getWorlds().firstOrNull()?.name
        ?: return "minecraft:${name.lowercase()}"

    return when {
        name == mainWorldName ->
            "minecraft:overworld"

        name == "${mainWorldName}_nether" && environment == World.Environment.NETHER ->
            "minecraft:the_nether"

        name == "${mainWorldName}_the_end" && environment == World.Environment.THE_END ->
            "minecraft:the_end"

        else ->
            "minecraft:${name.lowercase()}"
    }
}
