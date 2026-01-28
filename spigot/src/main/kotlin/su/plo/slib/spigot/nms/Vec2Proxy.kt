package su.plo.slib.spigot.nms

import xyz.jpenilla.reflectionremapper.proxy.annotation.FieldGetter
import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies
import xyz.jpenilla.reflectionremapper.proxy.annotation.Type

@Proxies(
    className = "net.minecraft.world.phys.Vec2"
)
interface Vec2Proxy {
    @FieldGetter("x")
    fun x(
        @Type(className = "net.minecraft.world.phys.Vec2") instance: Any,
    ): Float

    @FieldGetter("y")
    fun y(
        @Type(className = "net.minecraft.world.phys.Vec2") instance: Any,
    ): Float
}
