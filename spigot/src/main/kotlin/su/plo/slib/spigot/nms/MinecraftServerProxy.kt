package su.plo.slib.spigot.nms

import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies
import xyz.jpenilla.reflectionremapper.proxy.annotation.Type

@Proxies(
    className = "net.minecraft.server.MinecraftServer"
)
interface MinecraftServerProxy {
    fun getCommands(
        @Type(className = "net.minecraft.server.MinecraftServer") instance: Any
    ): Any
}
