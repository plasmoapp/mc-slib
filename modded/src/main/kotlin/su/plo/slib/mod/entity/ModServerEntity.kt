package su.plo.slib.mod.entity

import net.minecraft.world.entity.Entity
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.position.Pos3d
import su.plo.slib.api.server.position.ServerPos3d
import su.plo.slib.api.server.world.McServerWorld
import su.plo.slib.mod.extension.level
import java.util.*

open class ModServerEntity<E : Entity>(
    private val minecraftServer: McServerLib,
    var instance: E
) : McServerEntity {

    private val position = Pos3d()
    private val lookAngle = Pos3d()

    override val eyeHeight: Double
        get() = instance.eyeHeight.toDouble()

    override val hitBoxWidth: Float
        get() = instance.bbWidth

    override val hitBoxHeight: Float
        get() = instance.bbHeight

    override val id: Int
        get() = instance.id

    override val uuid: UUID
        get() = instance.uuid

    override val world: McServerWorld
        get() = minecraftServer.getWorld(instance.level())

    override fun isValid(): Boolean =
        instance.isAlive

    override fun getPosition(position: Pos3d): Pos3d {
        position.x = instance.position().x
        position.y = instance.position().y
        position.z = instance.position().z

        return position
    }

    override fun getLookAngle(lookAngle: Pos3d): Pos3d {
        lookAngle.x = instance.lookAngle.x
        lookAngle.y = instance.lookAngle.y
        lookAngle.z = instance.lookAngle.z

        return lookAngle
    }

    override fun getPosition() =
        getPosition(position)

    override fun getLookAngle() =
        getLookAngle(lookAngle)

    override fun getServerPosition(position: ServerPos3d): ServerPos3d {
        position.world = minecraftServer.getWorld(instance.level())

        position.x = instance.position().x
        position.y = instance.position().y
        position.z = instance.position().z

        position.yaw = instance.xRot
        position.pitch = instance.yRot

        return position
    }

    override fun getServerPosition() =
        ServerPos3d(
            minecraftServer.getWorld(instance.level()),
            instance.position().x(),
            instance.position().y(),
            instance.position().z(),
            instance.xRot,
            instance.yRot
        )

    @Suppress("UNCHECKED_CAST")
    override fun <T> getInstance() =
        instance as T
}
