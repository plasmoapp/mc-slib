package su.plo.slib.minestom

import net.kyori.adventure.key.Key
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.TranslationStore
import net.minecrell.terminalconsole.SimpleTerminalConsole
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.event.Event
import net.minestom.server.event.GlobalEventHandler
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import su.plo.slib.server.TestServer
import java.io.File
import java.text.MessageFormat
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.system.exitProcess
import kotlin.time.measureTime

private val logger = MinecraftServer.LOGGER

fun main() {
    val startTime = measureTime { startServer() }
    logger.info("Done (%.3fs)!".format(startTime.inWholeMilliseconds.toDouble() / 1000))
}

fun startServer() {
    val minecraftServer = MinecraftServer.init()

    registerVanillaTranslations()

    val minecraftServerLib = MinestomServerLib(File("slib-test"))
    val testServer = TestServer(minecraftServerLib)

    minecraftServerLib.onInitialize()
    testServer.onEnable()

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

    thread(isDaemon = true) {
        Console().start()
    }
}

class Console : SimpleTerminalConsole() {
    private val commandManager = MinecraftServer.getCommandManager()

    override fun isRunning(): Boolean =
        MinecraftServer.isStarted() && !MinecraftServer.isStopping()

    override fun runCommand(command: String) {
        try {
            commandManager.execute(commandManager.consoleSender, command)
        } catch (e: Throwable) {
            logger.error("Failed to execute command: /$command", e)
        }
    }

    override fun shutdown() {
        logger.info("Shutting down...")
        try {
            MinecraftServer.stopCleanly()
            exitProcess(0)
        } catch (e: Throwable) {
            logger.error("An error occurred while shutting down", e)
            exitProcess(1)
        }
    }

}

private inline fun <reified T : Event> GlobalEventHandler.addListener(crossinline listener: (T) -> Unit) {
    addListener(T::class.java) { listener.invoke(it) }
}

// Minestom doesn't ship vanilla translations, so translation keys leak to the console as raw ids
// Register the minimum set the smoke tests need
private fun registerVanillaTranslations() {
    val store = TranslationStore.messageFormat(Key.key("slib", "test"))
    store.defaultLocale(Locale.US)
    store.register("command.context.parse_error", Locale.US, MessageFormat("{0} at position {1}: {2}"))
    store.register("argument.uuid.invalid", Locale.US, MessageFormat("Invalid UUID"))
    GlobalTranslator.translator().addSource(store)
}
