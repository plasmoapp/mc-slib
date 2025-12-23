package su.plo.slib.spigot.nms

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import xyz.jpenilla.reflectionremapper.proxy.annotation.Proxies
import xyz.jpenilla.reflectionremapper.proxy.annotation.Static

@Proxies(
    className = "net.minecraft.commands.arguments.EntityArgument"
)
interface EntityArgumentProxy {
    @Static
    fun entity(): ArgumentType<Any>

    @Static
    fun entities(): ArgumentType<Any>

    @Static
    fun player(): ArgumentType<Any>

    @Static
    fun players(): ArgumentType<Any>

    @Static
    fun getEntity(context: CommandContext<*>, name: String): Any

    @Static
    fun getEntities(context: CommandContext<*>, name: String): Collection<Any>

    @Static
    fun getPlayer(context: CommandContext<*>, name: String): Any

    @Static
    fun getPlayers(context: CommandContext<*>, name: String): Collection<Any>
}
