package su.plo.slib.spigot.entity

import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import su.plo.slib.api.position.Pos3d
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.position.ServerPos3d
import su.plo.slib.api.server.world.McServerWorld
import su.plo.slib.spigot.SpigotServerLib
import java.util.*

open class SpigotServerEntity<E : LivingEntity>(
    protected val minecraftServer: SpigotServerLib,
    protected val instance: E
) : McServerEntity {

    private val position = Pos3d()
    private val lookAngle = Pos3d()

    private var location: Location? = null

    override val id: Int
        get() = instance.entityId

    override val uuid: UUID
        get() = instance.uniqueId

    override val eyeHeight: Double
        get() = instance.eyeHeight

    override val hitBoxWidth: Float
        get() = instance.boundingBox.widthX.toFloat()

    override val hitBoxHeight: Float
        get() = instance.boundingBox.height.toFloat()

    override val world: McServerWorld
        get() = minecraftServer.getWorld(instance.world)

    override fun getPosition() = getPosition(position)

    override fun getPosition(position: Pos3d): Pos3d {
        val location = fetchLocation()

        position.x = location.x
        position.y = location.y
        position.z = location.z

        return position
    }

    override fun getLookAngle() = getLookAngle(lookAngle)

    override fun getLookAngle(lookAngle: Pos3d): Pos3d {
        val vector = instance.location.direction

        lookAngle.x = vector.x
        lookAngle.y = vector.y
        lookAngle.z = vector.z

        return lookAngle
    }

    override fun <T> getInstance() = instance as T

    override fun getServerPosition(): ServerPos3d {
        val location = fetchLocation()

        return ServerPos3d(
            minecraftServer.getWorld(instance.world),
            location.x,
            location.y,
            location.z,
            location.yaw,
            location.pitch
        )
    }

    override fun getServerPosition(position: ServerPos3d): ServerPos3d {
        val location = fetchLocation()

        position.world = minecraftServer.getWorld(instance.world)
        position.x = location.x
        position.y = location.y
        position.z = location.z
        position.yaw = location.yaw
        position.pitch = location.pitch

        return position
    }

    private fun fetchLocation(): Location {
        if (location == null) {
            location = instance.location
        } else {
            instance.getLocation(location)
        }

        return location!!
    }
}
