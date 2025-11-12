package su.plo.slib.spigot.util

import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.util.Vector
import su.plo.slib.api.logging.McLoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

object GameEventUtil {

    private val gameEvents: MutableMap<String, Any> = HashMap()

    private val gameEventClass by lazy {
        try {
            Class.forName("org.bukkit.GameEvent")
        } catch (e: ClassNotFoundException) {
            sendGameEventNotSupported()
            null
        }
    }

    private val sendEntityGameEventMethod by lazy {
        try {
            World::class.java.getMethod(
                "sendGameEvent",
                Entity::class.java,
                gameEventClass,
                Vector::class.java
            )
        } catch (e: NoSuchMethodException) {
            sendGameEventNotSupported()
            null
        }
    }

    private val logger = McLoggerFactory.createLogger("GameEventUtil")
    private var sent: AtomicBoolean = AtomicBoolean(false)

    private fun sendGameEventNotSupported() {
        if (!sent.compareAndSet(false, true)) return
        logger.error("Game events are not supported on your platform. Update to 1.19.2+ and use Paper: https://papermc.io/.")
    }

    fun World.sendEntityGameEvent(paperEntity: Entity, gameEventName: String) {
        sendEntityGameEventMethod?.invoke(this, paperEntity, parseGameEvent(gameEventName), paperEntity.location.toVector())
    }

    fun parseGameEvent(gameEventName: String): Any =
        gameEvents.computeIfAbsent(gameEventName) {
            val gameEventKey: NamespacedKey
            val split = gameEventName.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            gameEventKey =
                if (split.size == 2) {
                    NamespacedKey.fromString(gameEventName)
                } else {
                    NamespacedKey.minecraft(gameEventName)
                }!!

            val gameEventClass = Class.forName("org.bukkit.GameEvent")
            val gameEventByKeyMethod = gameEventClass.getMethod("getByKey", NamespacedKey::class.java)

            gameEventByKeyMethod.invoke(null, gameEventKey) ?: gameEventClass.getField("STEP").get(null)
        }
}
