package su.plo.slib.paper.world

import org.bukkit.GameEvent
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.plugin.java.JavaPlugin
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.world.McServerWorld
import su.plo.slib.paper.extension.runSync
import java.util.Objects

class PaperServerWorld(
    private val loader: JavaPlugin,
    private val level: World
) : McServerWorld {

    override val name: String = level.name

    override val key: String = level.key.toString()

    override fun sendGameEvent(entity: McServerEntity, gameEvent: String) {
        val paperEntity = entity.getInstance<Entity>()

        val gameEvent = NamespacedKey.fromString(gameEvent)
            ?.let { Registry.GAME_EVENT[it] }
            ?: GameEvent.STEP

        loader.runSync(paperEntity) {
            // because `sendGameEvent` can be invoked async
            // entity's world can be different on the next thread tick
            // so entity's world is used
            paperEntity.world.sendGameEvent(
                paperEntity,
                gameEvent,
                paperEntity.location.toVector(),
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getInstance() = level as T

    override fun equals(other: Any?) =
        if (this === other) {
            true
        } else if (other != null && this.javaClass == other.javaClass) {
            val world = other as PaperServerWorld
            level === world.level
        } else {
            false
        }

    override fun hashCode() = Objects.hash(level)
}
