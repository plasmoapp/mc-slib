package su.plo.slib.minestom.world

import net.minestom.server.instance.Instance
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.world.McServerWorld
import java.util.*

class MinestomServerWorld(
    private val level: Instance
) : McServerWorld {

    override val name: String = level.uniqueId.toString()

    override fun sendGameEvent(entity: McServerEntity, gameEvent: String) {
        // Minestom doesn't support this (yet)
        return
    }

    override fun <T> getInstance() = level as T

    override fun equals(other: Any?) =
        if (this === other) {
            true
        } else if (other != null && this.javaClass == other.javaClass) {
            val world = other as MinestomServerWorld
            level === world.level
        } else {
            false
        }

    override fun hashCode() = Objects.hash(level)
}
