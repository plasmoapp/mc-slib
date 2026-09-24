package su.plo.slib.server

import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.event.permission.McPermissionsRegisterEvent
import su.plo.slib.api.event.player.McPlayerJoinEvent
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.permission.PermissionDefault
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent
import su.plo.slib.api.server.event.player.McPlayerRegisterChannelsEvent

class TestServer(
    val minecraftServer: McServerLib,
) {
    private var logger = McLoggerFactory.createLogger("TestServer")

    val channelKey = "slib:channels/test"

    init {
        minecraftServer.serverTranslator.defaultLanguage = UNTRANSLATED_LANGUAGE
        minecraftServer.serverTranslator.register(TEST_LANGUAGE, testTranslations)

        serverLib = minecraftServer
        registerCommands()

        McPermissionsRegisterEvent.registerListener { permissions ->
            permissions.register("slib.test", PermissionDefault.OP)
            logger.info("Permission 'slib.test' registered")
        }

        McPlayerJoinEvent.registerListener { player ->
            logger.info("Player ${player.name} joined the server")
        }

        McPlayerQuitEvent.registerListener { player ->
            logger.info("Player ${player.name} quit the server")
        }

        McPlayerRegisterChannelsEvent.registerListener { player, channels ->
            logger.info(
                "(${player.name}'s permissions) slib.test: {}",
                player.getPermission("slib.test"),
            )

            logger.info("Player ${player.name} registered channels: $channels")
        }

        McServerCommandsRegisterEvent.registerListener { commands, _ ->
            commands.logRegisteredCommands = true

            commands.register(
                "ping",
                object : McCommand {
                    override fun execute(
                        source: McCommandSource,
                        arguments: Array<String>,
                    ) {
                        source.sendMessage("Pong")
                    }
                },
            )
        }
    }

    fun onEnable() {
        minecraftServer.scheduler.runTask {
            logger.info("Message from main thread")
        }

        minecraftServer.worlds.forEach { world ->
            logger.info("World: name=${world.name}, key=${world.key}")
        }

        minecraftServer.channelManager.registerChannelHandler(channelKey.toString()) { player, data ->
            logger.info("Received channel #$channelKey message from ${player.name}: ${data.toString(Charsets.UTF_8)}")
        }
        logger.info("Channel handler registered: $channelKey")
    }
}
