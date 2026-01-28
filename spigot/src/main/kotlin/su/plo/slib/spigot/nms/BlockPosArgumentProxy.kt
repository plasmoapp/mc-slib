package su.plo.slib.spigot.nms

import com.mojang.brigadier.arguments.ArgumentType
import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies
import xyz.jpenilla.reflectionremapper.proxy.annotation.Static

@Proxies(
    className = "net.minecraft.commands.arguments.coordinates.BlockPosArgument"
)
interface BlockPosArgumentProxy {
    @Static
    fun blockPos(): ArgumentType<Any>
}
