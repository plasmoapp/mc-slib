package su.plo.slib.spigot.nms

import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies
import xyz.jpenilla.reflectionremapper.proxy.annotation.Type

@Proxies(
    className = "net.minecraft.commands.arguments.selector.EntitySelector"
)
interface EntitySelectorProxy {
    fun findSingleEntity(
        @Type(className = "net.minecraft.commands.arguments.selector") instance: Any,
        @Type(className = "net.minecraft.commands.CommandSourceStack") source: Any,
    ): Any

    fun findEntities(
        @Type(className = "net.minecraft.commands.arguments.selector") instance: Any,
        @Type(className = "net.minecraft.commands.CommandSourceStack") source: Any,
    ): List<Any>

    fun findSinglePlayer(
        @Type(className = "net.minecraft.commands.arguments.selector") instance: Any,
        @Type(className = "net.minecraft.commands.CommandSourceStack") source: Any,
    ): Any

    fun findPlayers(
        @Type(className = "net.minecraft.commands.arguments.selector") instance: Any,
        @Type(className = "net.minecraft.commands.CommandSourceStack") source: Any,
    ): List<Any>
}
