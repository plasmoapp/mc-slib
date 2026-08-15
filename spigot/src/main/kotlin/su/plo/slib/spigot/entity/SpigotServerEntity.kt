package su.plo.slib.spigot.entity

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import su.plo.slib.api.position.Pos3d
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.position.ServerPos3d
import su.plo.slib.api.server.world.McServerWorld
import su.plo.slib.spigot.SpigotServerLib
import java.util.UUID

open class SpigotServerEntity<E : Entity>(
    protected val minecraftServer: SpigotServerLib,
    protected val instance: E
) : McServerEntity {
    override val id: Int
        get() = instance.entityId

    override val uuid: UUID
        get() = instance.uniqueId

    override val eyeHeight: Double
        get() =
            if (instance is LivingEntity) {
                instance.eyeHeight
            } else {
                0.0
            }

    override val hitBoxWidth: Float
        get() = instance.boundingBox.widthX.toFloat()

    override val hitBoxHeight: Float
        get() = instance.boundingBox.height.toFloat()

    override val world: McServerWorld
        get() = minecraftServer.getWorld(instance.world)

    override fun isValid(): Boolean =
        instance.isValid

    override fun getPosition() = getPosition(Pos3d())

    override fun getPosition(position: Pos3d): Pos3d {
        position.x = instance.x
        position.y = instance.y
        position.z = instance.z

        return position
    }

    override fun getLookAngle() = getLookAngle(Pos3d())

    override fun getLookAngle(lookAngle: Pos3d): Pos3d {
        val vector = instance.location.direction

        lookAngle.x = vector.x
        lookAngle.y = vector.y
        lookAngle.z = vector.z

        return lookAngle
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getInstance() = instance as T

    override fun getServerPosition(): ServerPos3d {
        return ServerPos3d(
            minecraftServer.getWorld(instance.world),
            instance.x,
            instance.y,
            instance.z,
            instance.yaw,
            instance.pitch,
        )
    }

    override fun getServerPosition(position: ServerPos3d): ServerPos3d {
        position.world = minecraftServer.getWorld(instance.world)
        position.x = instance.x
        position.y = instance.y
        position.z = instance.z
        position.yaw = instance.yaw
        position.pitch = instance.pitch

        return position
    }
}
