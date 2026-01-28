package su.plo.slib.spigot.nms

import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies
import xyz.jpenilla.reflectionremapper.proxy.annotation.Type

@Proxies(
    className = "net.minecraft.world.phys.Vec3"
)
interface Vec3Proxy {
    fun x(
        @Type(className = "net.minecraft.world.phys.Vec3") instance: Any,
    ): Double

    fun y(
        @Type(className = "net.minecraft.world.phys.Vec3") instance: Any,
    ): Double

    fun z(
        @Type(className = "net.minecraft.world.phys.Vec3") instance: Any,
    ): Double
}
