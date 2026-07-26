package su.plo.slib.spigot.nms

import org.bukkit.World
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

    @FieldGetter("level")
    fun getLevel(
        @Type(className = "net.minecraft.commands.CommandSourceStack") instance: Any,
    ): Any?

    @FieldGetter("silent")
    fun isSilent(
        @Type(className = "net.minecraft.commands.CommandSourceStack") instance: Any,
    ): Boolean
}

fun CommandSourceStackProxy.getWorld(instance: Any): World? {
    val level = getLevel(instance) ?: return null

    val getWorldMethod = runCatching { level.javaClass.getMethod("getWorld") }
        .getOrNull()
        ?.takeIf { World::class.java.isAssignableFrom(it.returnType) }
        ?: return null

    return getWorldMethod.invoke(level) as? World
}
