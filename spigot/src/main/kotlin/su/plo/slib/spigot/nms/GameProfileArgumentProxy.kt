package su.plo.slib.spigot.nms

import com.mojang.brigadier.arguments.ArgumentType
import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies
import xyz.jpenilla.reflectionremapper.proxy.annotation.Static

@Proxies(
    className = "net.minecraft.commands.arguments.GameProfileArgument"
)
interface GameProfileArgumentProxy {
    @Static
    fun gameProfile(): ArgumentType<Any>
}
