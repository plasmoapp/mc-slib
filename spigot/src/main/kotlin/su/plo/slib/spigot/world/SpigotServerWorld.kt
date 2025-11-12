package su.plo.slib.spigot.world

import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.plugin.java.JavaPlugin
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.world.McServerWorld
import su.plo.slib.spigot.extension.runSync
import su.plo.slib.spigot.util.GameEventUtil.sendEntityGameEvent
import java.util.Objects

class SpigotServerWorld(
    private val loader: JavaPlugin,
    private val level: World
) : McServerWorld {

    override val name: String = level.name

    override fun sendGameEvent(entity: McServerEntity, gameEvent: String) {
        val paperEntity = entity.getInstance<Entity>()

        loader.runSync(paperEntity) {
            // because `sendGameEvent` can be invoked async
            // entity's world can be different on the next thread tick
            // so entity's world is used
            paperEntity.world.sendEntityGameEvent(paperEntity, gameEvent)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getInstance() = level as T

    override fun equals(other: Any?) =
        if (this === other) {
            true
        } else if (other != null && this.javaClass == other.javaClass) {
            val world = other as SpigotServerWorld
            level === world.level
        } else {
            false
        }

    override fun hashCode() = Objects.hash(level)
}
