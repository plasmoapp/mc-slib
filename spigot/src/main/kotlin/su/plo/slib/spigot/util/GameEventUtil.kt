package su.plo.slib.spigot.util

import org.bukkit.NamespacedKey

object GameEventUtil {

    fun parseGameEvent(gameEventName: String): Any {
        val gameEventKey: NamespacedKey
        val split = gameEventName.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        gameEventKey = if (split.size == 2) {
            NamespacedKey(split[0], split[1])
        } else {
            NamespacedKey("minecraft", gameEventName)
        }

        val gameEventClass = Class.forName("org.bukkit.GameEvent")
        val gameEventByKeyMethod = gameEventClass.getMethod("getByKey", NamespacedKey::class.java)

        return gameEventByKeyMethod.invoke(null, gameEventKey)
            ?: return gameEventClass.getField("STEP").get(null)
    }
}
