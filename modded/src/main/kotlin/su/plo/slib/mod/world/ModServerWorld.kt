package su.plo.slib.mod.world

import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.gameevent.GameEvent
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.world.McServerWorld
import java.util.Objects

class ModServerWorld(
    private val level: ServerLevel
) : McServerWorld {

    override val name: String = level.dimension().location().toString()

    override val key: String = name

    override fun sendGameEvent(entity: McServerEntity, gameEvent: String) {
        val serverEntity = entity.getInstance<Entity>()

        level.server.execute {
            level.gameEvent(serverEntity, parseGameEvent(gameEvent), serverEntity.position())
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getInstance(): T {
        return level as T
    }

    override fun equals(other: Any?): Boolean {
        return if (this === other) {
            true
        } else if (other != null && this.javaClass == other.javaClass) {
            val world = other as ModServerWorld
            level === world.level
        } else {
            false
        }
    }

    override fun hashCode() =
        Objects.hash(level)

    private fun parseGameEvent(gameEventName: String): Holder.Reference<GameEvent> =
    //? if >=1.21.2 {
        /*BuiltInRegistries.GAME_EVENT.get(ResourceLocation.tryParse(gameEventName)!!)
            .orElseThrow { IllegalArgumentException("Invalid game event") }
    *///?} else {
        BuiltInRegistries.GAME_EVENT.getHolder(ResourceLocation.tryParse(gameEventName)!!)
            .orElseThrow { IllegalArgumentException("Invalid game event") }
    //?}
}
