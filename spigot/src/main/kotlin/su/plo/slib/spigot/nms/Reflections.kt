package su.plo.slib.spigot.nms

import com.mojang.brigadier.CommandDispatcher
import org.bukkit.Bukkit
import org.bukkit.Server
import org.semver4j.Semver
import xyz.jpenilla.reflectionremapper.ReflectionRemapper
import xyz.jpenilla.reflectionremapper.proxy.ReflectionProxyFactory

object ReflectionProxies {
    val minecraftServer: MinecraftServerProxy
    val commands: CommandsProxy
    val commandSourceStack: CommandSourceStackProxy
    val entity: EntityProxy
    val entityArgument: EntityArgumentProxy

    init {
        val bukkitVersion = Bukkit.getVersion()
        val minecraftVersionString = bukkitVersion.substring(bukkitVersion.lastIndexOf(" ") + 1, bukkitVersion.length - 1)
        val minecraftVersion = Semver(minecraftVersionString)

        val remapper =
            try {
                ReflectionRemapper.forReobfMappingsInPaperJar()
            } catch (_: Throwable) {
                val mappingsVersion = listOf(
                    "1.21.6",
                    "1.21.4",
                    "1.20.4",
                    "1.17.1",
                    "1.16.5",
                )
                    .firstOrNull { minecraftVersion.satisfies(">=$it") }
                    ?:  throw IllegalStateException("$minecraftVersionString is not supported")

                ReflectionRemapper.forMappings(
                    javaClass.classLoader.getResourceAsStream("mappings/$mappingsVersion.tiny")!!,
                    "source",
                    "target",
                )
            }

        val proxyFactory = ReflectionProxyFactory.create(remapper, javaClass.classLoader)

        minecraftServer = proxyFactory.reflectionProxy()
        commands = proxyFactory.reflectionProxy()
        commandSourceStack = proxyFactory.reflectionProxy()
        entity = proxyFactory.reflectionProxy()
        entityArgument = proxyFactory.reflectionProxy()
    }

    private inline fun <reified T> ReflectionProxyFactory.reflectionProxy() =
        reflectionProxy(T::class.java)
}

fun Server.getCommandDispatcher(): CommandDispatcher<Any> {
    val minecraftServer = getMinecraftServer()
    val commands = ReflectionProxies.minecraftServer.getCommands(minecraftServer)
    val dispatcher = ReflectionProxies.commands.getDispatcher(commands)

    return dispatcher
}

fun Server.getMinecraftServer(): Any {
    val getServerMethod = javaClass.getMethod("getServer")
    val minecraftServer = getServerMethod.invoke(this)

    return minecraftServer
}
