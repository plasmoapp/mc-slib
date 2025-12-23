package su.plo.slib.minestom

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.event.Event
import net.minestom.server.event.GlobalEventHandler
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.server.TestServer
import java.io.File
import kotlin.time.measureTime

private val logger = McLoggerFactory.createLogger("Server")

fun main() {
    val startTime = measureTime { startServer() }
    logger.info("Done (%.3fs)!".format(startTime.inWholeMilliseconds.toDouble() / 1000))
}

fun startServer() {
    val minecraftServer = MinecraftServer.init()

    val minecraftServerLib = MinestomServerLib(File("slib-test"))
    val testServer = TestServer(minecraftServerLib)

    minecraftServerLib.onInitialize()
    testServer.registerChannels()

    val instanceManager = MinecraftServer.getInstanceManager()
    val instanceContainer = instanceManager.createInstanceContainer()

    MinecraftServer.getGlobalEventHandler().apply {
        addListener<AsyncPlayerConfigurationEvent> { event ->
            val player = event.player
            player.respawnPoint = Pos(0.0, 0.0, 0.0)
            event.spawningInstance = instanceContainer
        }

        addListener<PlayerSpawnEvent> { event ->
            val player = event.player
            player.gameMode = GameMode.SPECTATOR
        }
    }

    minecraftServer.start("0.0.0.0", 25565)
}

private inline fun <reified T : Event> GlobalEventHandler.addListener(crossinline listener: (T) -> Unit) {
    addListener(T::class.java) { listener.invoke(it) }
}
