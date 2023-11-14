package su.plo.slib.spigot.util

import org.bukkit.GameEvent
import org.bukkit.NamespacedKey

object GameEventUtil {

    fun parseGameEvent(gameEventName: String): GameEvent {
        val gameEventKey: NamespacedKey
        val split = gameEventName.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        gameEventKey = if (split.size == 2) {
            NamespacedKey(split[0], split[1])
        } else {
            NamespacedKey("minecraft", gameEventName)
        }

        return GameEvent.getByKey(gameEventKey) ?: return GameEvent.STEP
    }
}
