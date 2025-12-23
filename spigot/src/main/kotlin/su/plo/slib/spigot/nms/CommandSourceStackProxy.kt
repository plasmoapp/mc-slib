package su.plo.slib.spigot.nms

import org.bukkit.command.CommandSender
import xyz.jpenilla.reflectionremapper.proxy.annotation.FieldGetter
import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies
import xyz.jpenilla.reflectionremapper.proxy.annotation.Type

@Proxies(
    className = "net.minecraft.commands.CommandSourceStack"
)
interface CommandSourceStackProxy {
    fun getBukkitSender(
        @Type(className = "net.minecraft.commands.CommandSourceStack") instance: Any,
    ): CommandSender

    @FieldGetter("entity")
    fun getEntity(
        @Type(className = "net.minecraft.commands.CommandSourceStack") instance: Any,
    ): Any?
}
