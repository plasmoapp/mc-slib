package su.plo.slib.spigot.nms

import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies
import xyz.jpenilla.reflectionremapper.proxy.annotation.Type

@Proxies(
    className = "net.minecraft.commands.arguments.coordinates.Coordinates"
)
interface CoordinatesProxy {
    fun getPosition(
        @Type(className = "net.minecraft.commands.arguments.selector.EntitySelector") instance: Any,
        @Type(className = "net.minecraft.commands.CommandSourceStack") source: Any,
    ): Any

    fun getRotation(
        @Type(className = "net.minecraft.commands.arguments.selector.EntitySelector") instance: Any,
        @Type(className = "net.minecraft.commands.CommandSourceStack") source: Any,
    ): Any
}
