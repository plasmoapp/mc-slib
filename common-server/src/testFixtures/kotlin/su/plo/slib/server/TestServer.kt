package su.plo.slib.server

import com.mojang.brigadier.Command
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.event.player.McPlayerJoinEvent
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.command.brigadier.McArgumentTypes
import su.plo.slib.api.server.command.brigadier.McEntitiesArgumentResolver
import su.plo.slib.api.server.command.brigadier.McEntityArgumentResolver
import su.plo.slib.api.server.command.brigadier.McGameProfilesArgumentResolver
import su.plo.slib.api.server.command.brigadier.McPlayerArgumentResolver
import su.plo.slib.api.server.command.brigadier.McPlayersArgumentResolver
import su.plo.slib.api.server.command.brigadier.ServerPos3dResolver
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent
import su.plo.slib.api.server.event.player.McPlayerRegisterChannelsEvent
import su.plo.slib.server.command.UuidArgumentType
import java.util.UUID

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
                McCommandManager.literal("brigadier-custom-type")
                    .then(
                        McCommandManager.argument("uuid", UuidArgumentType())
                            .executes {
                                val uuid = it.getArgument<UUID>("uuid", UUID::class.java)

                                it.source.source.sendMessage(uuid.toString())

                                Command.SINGLE_SUCCESS
                            }
                    )
            )

            commands.register(
                McCommandManager.literal("brigadier-entity-selector")
                    .then(
                        McCommandManager.literal("entity")
                            .then(
                                McCommandManager.argument("target", McArgumentTypes.entity())
                                    .executes {
                                        val resolver = it.getArgument<McEntityArgumentResolver>(
                                            "target",
                                            McEntityArgumentResolver::class.java,
                                        )
                                        val entity = resolver.resolve(it.source)

                                        val source = it.source
                                        source.source.sendMessage("Found entity: $entity; Source: ${source.source}; Executor: ${source.executor}")

                                        Command.SINGLE_SUCCESS
                                    }
                            )
                    )
                    .then(
                        McCommandManager.literal("entities")
                            .then(
                                McCommandManager.argument("target", McArgumentTypes.entities())
                                    .executes {
                                        val resolver = it.getArgument<McEntitiesArgumentResolver>(
                                            "target",
                                            McEntitiesArgumentResolver::class.java,
                                        )
                                        val entities = resolver.resolve(it.source)

                                        val source = it.source
                                        source.source.sendMessage("Found entities: $entities; Source: ${source.source}; Executor: ${source.executor}")

                                        Command.SINGLE_SUCCESS
                                    }
                            )
                    )
                    .then(
                        McCommandManager.literal("player")
                            .then(
                                McCommandManager.argument("target", McArgumentTypes.player())
                                    .executes {
                                        val resolver = it.getArgument<McPlayerArgumentResolver>(
                                            "target",
                                            McPlayerArgumentResolver::class.java,
                                        )
                                        val player = resolver.resolve(it.source)

                                        val source = it.source
                                        source.source.sendMessage("Found player: $player; Source: ${source.source}; Executor: ${source.executor}")

                                        Command.SINGLE_SUCCESS
                                    }
                            )
                    )
                    .then(
                        McCommandManager.literal("players")
                            .then(
                                McCommandManager.argument("target", McArgumentTypes.players())
                                    .executes {
                                        val resolver = it.getArgument<McPlayersArgumentResolver>(
                                            "target",
                                            McPlayersArgumentResolver::class.java,
                                        )
                                        val players = resolver.resolve(it.source)

                                        val source = it.source
                                        source.source.sendMessage("Found players: $players; Source: ${source.source}; Executor: ${source.executor}")

                                        Command.SINGLE_SUCCESS
                                    }
                            )
                    )
            )

            commands.register(
                McCommandManager.literal("brigadier-game-profiles-selector")
                    .then(
                        McCommandManager.argument("targets", McArgumentTypes.gameProfiles())
                            .executes {
                                val resolver = it.getArgument<McGameProfilesArgumentResolver>(
                                    "targets",
                                    McGameProfilesArgumentResolver::class.java,
                                )
                                val gameProfiles = resolver.resolve(it.source)

                                val source = it.source
                                source.source.sendMessage("Found game profiles: $gameProfiles; Source: ${source.source}; Executor: ${source.executor}")

                                Command.SINGLE_SUCCESS
                            }
                    )
            )

            commands.register(
                McCommandManager.literal("brigadier-position-selector")
                    .then(
                        McCommandManager.argument("position", McArgumentTypes.position())
                            .executes {
                                val resolver = it.getArgument<ServerPos3dResolver>(
                                    "position",
                                    ServerPos3dResolver::class.java,
                                )
                                val position = resolver.resolve(it.source)

                                val source = it.source
                                source.source.sendMessage("Position: $position; Source: ${source.source}; Executor: ${source.executor}")

                                Command.SINGLE_SUCCESS
                            }
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
