package su.plo.slib.spigot.nms

import org.bukkit.entity.Entity
import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies
import xyz.jpenilla.reflectionremapper.proxy.annotation.Type

@Proxies(
    className = "net.minecraft.world.entity.Entity"
)
interface EntityProxy {
    fun getBukkitEntity(
        @Type(className = "net.minecraft.world.entity.Entity") instance: Any,
    ): Entity
}
