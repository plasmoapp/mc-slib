package su.plo.slib.spigot.entity

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import su.plo.slib.api.position.Pos3d
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.position.ServerPos3d
import su.plo.slib.api.server.world.McServerWorld
import su.plo.slib.spigot.SpigotServerLib
import su.plo.slib.spigot.util.extension.readPosition
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

    override fun getPosition(position: Pos3d): Pos3d =
        instance.readPosition(position)

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

    override fun getServerPosition(): ServerPos3d =
        getServerPosition(ServerPos3d())

    override fun getServerPosition(position: ServerPos3d): ServerPos3d {
        position.world = minecraftServer.getWorld(instance.world)

        return instance.readPosition(position)
    }
}
