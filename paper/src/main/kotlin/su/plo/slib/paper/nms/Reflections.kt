package su.plo.slib.paper.nms

import com.mojang.brigadier.CommandDispatcher
import org.bukkit.Server
import xyz.jpenilla.reflectionremapper.ReflectionRemapper
import xyz.jpenilla.reflectionremapper.proxy.ReflectionProxyFactory

object ReflectionProxies {
    val commandsClass: Class<*>
    val commandSourceStack: CommandSourceStackProxy
    val entity: EntityProxy
    val entityArgument: EntityArgumentProxy
    val entitySelector: EntitySelectorProxy
    val blockPosArgument: BlockPosArgumentProxy
    val coordinates: CoordinatesProxy
    val vec3Proxy: Vec3Proxy
    val vec2Proxy: Vec2Proxy
    val gameProfileArgument: GameProfileArgumentProxy

    init {
        val remapper = ReflectionRemapper.forReobfMappingsInPaperJar()

        val proxyFactory = ReflectionProxyFactory.create(remapper, javaClass.classLoader)

        commandsClass = Class.forName(remapper.remapClassName("net.minecraft.commands.Commands"))
        commandSourceStack = proxyFactory.reflectionProxy()
        entity = proxyFactory.reflectionProxy()
        entityArgument = proxyFactory.reflectionProxy()
        entitySelector = proxyFactory.reflectionProxy()
        blockPosArgument = proxyFactory.reflectionProxy()
        coordinates = proxyFactory.reflectionProxy()
        vec3Proxy = proxyFactory.reflectionProxy()
        vec2Proxy = proxyFactory.reflectionProxy()
        gameProfileArgument = proxyFactory.reflectionProxy()
    }

    private inline fun <reified T> ReflectionProxyFactory.reflectionProxy() =
        reflectionProxy(T::class.java)
}

@Suppress("UNCHECKED_CAST")
fun Server.getCommandDispatcher(): CommandDispatcher<Any> {
    val minecraftServer = getMinecraftServer()

    val getCommandsMethod = minecraftServer.javaClass.methods
        .first { it.returnType == ReflectionProxies.commandsClass }

    val commands = getCommandsMethod.invoke(minecraftServer)

    val dispatcherField = commands.javaClass.declaredFields
        .first { it.type == CommandDispatcher::class.java }
    dispatcherField.isAccessible = true

    val dispatcher = dispatcherField.get(commands)

    return dispatcher as CommandDispatcher<Any>
}

fun Server.getMinecraftServer(): Any {
    val getServerMethod = javaClass.getMethod("getServer")
    val minecraftServer = getServerMethod.invoke(this)

    return minecraftServer
}
