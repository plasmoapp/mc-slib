package su.plo.slib.mod.world

import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.world.McServerWorld
import java.util.*

//? if >=1.19 {
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.gameevent.GameEvent

//? if >=1.19.3 {
import net.minecraft.core.registries.BuiltInRegistries
//?} else {
/*import net.minecraft.core.Registry
*///?}

//? if >=1.20.5 {
/*import net.minecraft.core.Holder
*///?}
//?}

class ModServerWorld(
    private val level: ServerLevel
) : McServerWorld {

    override val name: String = level.dimension().location().toString()

    override fun sendGameEvent(entity: McServerEntity, gameEvent: String) {
        //? if >=1.19 {
        val serverEntity = entity.getInstance<Entity>()
        level.server.execute {
            level.gameEvent(serverEntity, parseGameEvent(gameEvent), serverEntity.position())
        }
        //?}
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

    //? if >=1.20.5 {
    /*private fun parseGameEvent(gameEventName: String): Holder.Reference<GameEvent> =
    //? if >=1.21.2 {
        /^BuiltInRegistries.GAME_EVENT.get(ResourceLocation.tryParse(gameEventName)!!)
            .orElseThrow { IllegalArgumentException("Invalid game event") }
    ^///?} else {
        BuiltInRegistries.GAME_EVENT.getHolder(ResourceLocation.tryParse(gameEventName)!!)
            .orElseThrow { IllegalArgumentException("Invalid game event") }
    //?}
    *///?} elif >=1.19 {
    private fun parseGameEvent(gameEventName: String): GameEvent {
        //? if >=1.19.3 {
        return BuiltInRegistries.GAME_EVENT[ResourceLocation.tryParse(gameEventName)]
        //?} else {
        /*return Registry.GAME_EVENT.get(ResourceLocation.tryParse(gameEventName))
        *///?}
    }
    //?}
}
