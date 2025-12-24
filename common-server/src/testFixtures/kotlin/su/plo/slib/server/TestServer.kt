package su.plo.slib.server

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.event.player.McPlayerJoinEvent
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.command.brigadier.McArgumentResolver
import su.plo.slib.api.server.command.brigadier.McArgumentTypes
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent
import su.plo.slib.api.server.event.player.McPlayerRegisterChannelsEvent

class TestServer(
    val minecraftServer: McServerLib,
) {
    private var logger = McLoggerFactory.createLogger("TestServer")

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

            commands.register(
                LiteralArgumentBuilder.literal<Any>("brigadier-entity-selector")
                    .then(
                        LiteralArgumentBuilder.literal<Any>("entity")
                            .then(
                                RequiredArgumentBuilder.argument<Any, Any>("target", McArgumentTypes.entity())
                                    .executes {
                                        val entity = McArgumentResolver.getEntity(it, "target")

                                        val source = McBrigadierSource.from(it)
                                        source.source.sendMessage("Found entity: $entity; Source: ${source.source}; Executor: ${source.executor}")

                                        Command.SINGLE_SUCCESS
                                    }
                            )
                    )
                    .then(
                        LiteralArgumentBuilder.literal<Any>("entities")
                            .then(
                                RequiredArgumentBuilder.argument<Any, Any>("target", McArgumentTypes.entities())
                                    .executes {
                                        val entities = McArgumentResolver.getEntities(it, "target")

                                        val source = McBrigadierSource.from(it)
                                        source.source.sendMessage("Found entities: $entities; Source: ${source.source}; Executor: ${source.executor}")

                                        Command.SINGLE_SUCCESS
                                    }
                            )
                    )
                    .then(
                        LiteralArgumentBuilder.literal<Any>("player")
                            .then(
                                RequiredArgumentBuilder.argument<Any, Any>("target", McArgumentTypes.player())
                                    .executes {
                                        val player = McArgumentResolver.getPlayer(it, "target")

                                        val source = McBrigadierSource.from(it)
                                        source.source.sendMessage("Found player: $player; Source: ${source.source}; Executor: ${source.executor}")

                                        Command.SINGLE_SUCCESS
                                    }
                            )
                    )
                    .then(
                        LiteralArgumentBuilder.literal<Any>("players")
                            .then(
                                RequiredArgumentBuilder.argument<Any, Any>("target", McArgumentTypes.players())
                                    .executes {
                                        val players = McArgumentResolver.getPlayers(it, "target")

                                        val source = McBrigadierSource.from(it)
                                        source.source.sendMessage("Found players: $players; Source: ${source.source}; Executor: ${source.executor}")

                                        Command.SINGLE_SUCCESS
                                    }
                            )
                    )
            )
        }
    }

    fun registerChannels() {
        minecraftServer.channelManager.registerChannelHandler(channelKey.toString()) { player, data ->
            logger.info("Received channel #$channelKey message from ${player.name}: ${data.toString(Charsets.UTF_8)}")
        }
    }
}
