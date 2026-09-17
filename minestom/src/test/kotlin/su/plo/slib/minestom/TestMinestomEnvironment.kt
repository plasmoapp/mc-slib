package su.plo.slib.minestom

import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandManager
import su.plo.slib.server.TestServer
import java.io.File

internal object TestMinestomEnvironment {
    val commandManager: CommandManager by lazy {
        MinecraftServer.init()

        val serverLib = MinestomServerLib(File("build/tmp/slib-test"))
        TestServer(serverLib)
        serverLib.onInitialize()

        MinecraftServer.getCommandManager()
    }
}
