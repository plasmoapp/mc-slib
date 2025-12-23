package su.plo.slib.spigot.nms

import com.mojang.brigadier.CommandDispatcher
import org.bukkit.Bukkit
import org.bukkit.Server
import org.semver4j.Semver
import su.plo.slib.api.logging.McLoggerFactory
import xyz.jpenilla.reflectionremapper.ReflectionRemapper
import xyz.jpenilla.reflectionremapper.proxy.ReflectionProxyFactory

object ReflectionProxies {
    private val logger = McLoggerFactory.createLogger("ReflectionProxies")

    val commandsClass: Class<*>
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
                    .also { logger.info("Using mappings from paper jar") }
            } catch (_: Throwable) {
                val mappingsVersion = listOf(
                    "1.21.6",
                    "1.19.2",
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
                    .also {
                        logger.info("Using mappings from resources: \"mappings/$mappingsVersion.tiny\"")
                    }
            }

        val proxyFactory = ReflectionProxyFactory.create(remapper, javaClass.classLoader)

        commandsClass = Class.forName(remapper.remapClassName("net.minecraft.commands.Commands"))
        commandSourceStack = proxyFactory.reflectionProxy()
        entity = proxyFactory.reflectionProxy()
        entityArgument = proxyFactory.reflectionProxy()
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
