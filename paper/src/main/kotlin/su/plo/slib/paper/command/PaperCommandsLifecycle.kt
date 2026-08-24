package su.plo.slib.paper.command

import org.bukkit.plugin.java.JavaPlugin
import su.plo.slib.api.logging.McLogger
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

internal fun registerPaperCommandLifecycleEvent(loader: JavaPlugin, logger: McLogger, onCommandsRebuild: () -> Unit): Boolean {
    val lifecycleManager =
        try {
            loader.javaClass.getMethod("getLifecycleManager").invoke(loader)
        } catch (e: Throwable) {
            logger.debug("Paper commands lifecycle is not available", e)
            return false
        }

    return try {
        val eventType = Class.forName("io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents")
            .getField("COMMANDS")
            .get(null)

        val handlerClass = Class.forName("io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler")
        val handler = Proxy.newProxyInstance(
            handlerClass.classLoader,
            arrayOf(handlerClass),
            InvocationHandler { proxy, method, args ->
                when (method.name) {
                    "run" -> onCommandsRebuild()
                    "equals" -> return@InvocationHandler proxy === args?.firstOrNull()
                    "hashCode" -> return@InvocationHandler System.identityHashCode(proxy)
                    "toString" -> return@InvocationHandler "SlibCommandsLifecycleHandler"
                }

                null
            },
        )

        Class.forName("io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager")
            .methods
            .first { it.name == "registerEventHandler" && it.parameterCount == 2 }
            .invoke(lifecycleManager, eventType, handler)

        true
    } catch (e: Throwable) {
        logger.warn("Failed to hook into the paper commands lifecycle", e)
        false
    }
}
