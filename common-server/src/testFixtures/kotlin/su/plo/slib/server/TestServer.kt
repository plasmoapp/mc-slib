package su.plo.slib.server

import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.event.player.McPlayerJoinEvent
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent
import su.plo.slib.api.server.event.player.McPlayerRegisterChannelsEvent

class TestServer(
    val minecraftServer: McServerLib,
) {
    private var logger = McLoggerFactory.createLogger("TestMod")

    val channelKey = "slib:channels/test"

    init {
        McPlayerJoinEvent.registerListener { player ->
            logger.info("Player ${player.name} joined the server")
        }

        McPlayerQuitEvent.registerListener { player ->
            logger.info("Player ${player.name} quit the server")
        }

        McPlayerRegisterChannelsEvent.registerListener { player, channels ->
            logger.info("Player ${player.name} registered channels: $channels")
        }

        McServerCommandsRegisterEvent.registerListener { commands, _ ->
            commands.register("ping", object : McCommand {
                override fun execute(
                    source: McCommandSource,
                    arguments: Array<String>,
                ) {
                    source.sendMessage("Pong")
                }
            })
            logger.info("Command 'ping' registered")
        }
    }

    fun registerChannels() {
        minecraftServer.channelManager.registerChannelHandler(channelKey.toString()) { player, data ->
            logger.info("Received channel #$channelKey message from ${player.name}: ${data.toString(Charsets.UTF_8)}")
        }
    }
}
