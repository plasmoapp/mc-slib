package su.plo.slib.mod.world

import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.world.McServerWorld
import java.util.*

//#if MC>=11900

import net.minecraft.world.entity.Entity
import net.minecraft.world.level.gameevent.GameEvent

//#if MC>=11903
import net.minecraft.core.registries.BuiltInRegistries
//#else
//$$ import net.minecraft.core.Registry
//#endif

//#if MC>=12005
//$$ import net.minecraft.core.Holder
//#endif

//#endif

class ModServerWorld(
    private val level: ServerLevel
) : McServerWorld {

    override val name: String = level.dimension().location().toString()

    override fun sendGameEvent(entity: McServerEntity, gameEvent: String) {
        //#if MC>=11900
        val serverEntity = entity.getInstance<Entity>()
        level.server.execute {
            level.gameEvent(serverEntity, parseGameEvent(gameEvent), serverEntity.position())
        }
        //#endif
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

    //#if MC>=12005
    //$$ private fun parseGameEvent(gameEventName: String): Holder.Reference<GameEvent> =
    //$$     BuiltInRegistries.GAME_EVENT.getHolder(ResourceLocation.tryParse(gameEventName))
    //$$         .orElseThrow { IllegalArgumentException("Invalid game event") }
    //#elseif MC>=11900
    private fun parseGameEvent(gameEventName: String): GameEvent {
        //#if MC>=11903
        return BuiltInRegistries.GAME_EVENT[ResourceLocation.tryParse(gameEventName)]
        //#else
        //$$ return Registry.GAME_EVENT.get(ResourceLocation.tryParse(gameEventName))
        //#endif
    }
    //#endif
}
