package su.plo.slib.minestom.entity

import net.minestom.server.entity.LivingEntity
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.position.Pos3d
import su.plo.slib.api.server.position.ServerPos3d
import su.plo.slib.api.server.world.McServerWorld
import java.util.*

open class MinestomServerEntity<E : LivingEntity>(
    protected val minecraftServer: McServerLib,
    protected val instance: E
) : McServerEntity {

    private val position = Pos3d()
    private val lookAngle = Pos3d()

    override val id: Int
        get() = instance.entityId

    override val uuid: UUID
        get() = instance.uuid

    override val eyeHeight: Double
        get() = instance.eyeHeight

    override val hitBoxWidth: Float
        get() = instance.boundingBox.width().toFloat()

    override val hitBoxHeight: Float
        get() = instance.boundingBox.height().toFloat()

    override val world: McServerWorld
        get() = minecraftServer.getWorld(instance.instance)

    override fun isValid(): Boolean =
        !instance.isRemoved

    override fun getPosition() = getPosition(position)

    override fun getPosition(position: Pos3d): Pos3d {
        val location = instance.position

        position.x = location.x
        position.y = location.y
        position.z = location.z

        return position
    }

    override fun getLookAngle() = getLookAngle(lookAngle)

    override fun getLookAngle(lookAngle: Pos3d): Pos3d {
        val vector = instance.position.direction()

        lookAngle.x = vector.x
        lookAngle.y = vector.y
        lookAngle.z = vector.z

        return lookAngle
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getInstance() = instance as T

    override fun getServerPosition(): ServerPos3d {
        val location = instance.position

        return ServerPos3d(
            minecraftServer.getWorld(instance.instance),
            location.x,
            location.y,
            location.z,
            location.yaw,
            location.pitch
        )
    }

    override fun getServerPosition(position: ServerPos3d): ServerPos3d {
        val location = instance.position

        position.world = minecraftServer.getWorld(instance.instance)
        position.x = location.x
        position.y = location.y
        position.z = location.z
        position.yaw = location.yaw
        position.pitch = location.pitch

        return position
    }
}
