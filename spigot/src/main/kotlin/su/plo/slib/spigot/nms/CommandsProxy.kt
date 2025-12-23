package su.plo.slib.spigot.nms

import com.mojang.brigadier.CommandDispatcher
import xyz.jpenilla.reflectionremapper.proxy.annotation.FieldGetter
import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies
import xyz.jpenilla.reflectionremapper.proxy.annotation.Type

@Proxies(
    className = "net.minecraft.commands.Commands"
)
interface CommandsProxy {
    @FieldGetter("dispatcher")
    fun getDispatcher(
        @Type(className = "net.minecraft.commands.Commands") instance: Any,
    ): CommandDispatcher<Any>
}
