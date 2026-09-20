package su.plo.slib.server

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import dev.apehum.mcdsl.command.literalCommand
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.command.brigadier.McTextMessage
import su.plo.slib.api.event.command.McBrigadierCommandsRegisterEvent
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.command.brigadier.McArgumentTypes
import su.plo.slib.server.command.NestedGameProfilesArgumentType
import su.plo.slib.server.command.NestedGameProfilesTarget
import su.plo.slib.server.command.TranslatedArgumentType
import su.plo.slib.server.command.UuidArgumentType
import java.util.concurrent.atomic.AtomicBoolean

private val registered = AtomicBoolean()
private val logger = McLoggerFactory.createLogger("TestServerCommands")

internal lateinit var serverLib: McServerLib

private val commandFailed = DynamicCommandExceptionType { value ->
    McTextMessage.of(
        McTextComponent.translatable(FAILED_COMMAND_KEY, McTextComponent.literal(value.toString()))
    )
}

fun registerCommands() {
    if (!registered.compareAndSet(false, true)) return

    McBrigadierCommandsRegisterEvent.registerListener { registry ->
        registry.register(
            literalCommand("brigadier-custom-type") {
                val uuid by argument("uuid", UuidArgumentType())

                executes {
                    source.source.sendMessage(uuid.toString())
                }
            }
        )

        registry.register(
            literalCommand("brigadier-server-translation") {
                val value by argument("value", TranslatedArgumentType())

                executes {
                    throw commandFailed.create(value)
                }
            }
        )

        registry.register(
            literalCommand("brigadier-entity-selector") {
                literalCommand("entity") {
                    val target by argument("target", McArgumentTypes.entity())

                    executes {
                        val entity = target.resolve(source)

                        source.source.sendMessage("Found entity: $entity; Source: ${source.source}; Executor: ${source.executor}")
                    }
                }

                literalCommand("entities") {
                    val target by argument("target", McArgumentTypes.entities())

                    executes {
                        val entities = target.resolve(source)

                        source.source.sendMessage("Found entities: $entities; Source: ${source.source}; Executor: ${source.executor}")
                    }
                }

                literalCommand("player") {
                    val target by argument("target", McArgumentTypes.player())

                    executes {
                        val player = target.resolve(source)

                        source.source.sendMessage("Found player: $player; Source: ${source.source}; Executor: ${source.executor}")
                    }
                }

                literalCommand("players") {
                    val target by argument("target", McArgumentTypes.players())

                    executes {
                        val players = target.resolve(source)

                        source.source.sendMessage("Found players: $players; Source: ${source.source}; Executor: ${source.executor}")
                    }
                }
            }
        )

        registry.register(
            literalCommand("brigadier-game-profiles-selector") {
                val targets by argument("targets", McArgumentTypes.gameProfiles())

                executes {
                    val gameProfiles = targets.resolve(source)

                    source.source.sendMessage("Found game profiles: $gameProfiles; Source: ${source.source}; Executor: ${source.executor}")
                }
            }
        )

        registry.register(
            literalCommand("game-profile") {
                literalCommand("uuid") {
                    val uuid by argument("uuid", UuidArgumentType())

                    executes {
                        source.sendFeedback("Game profile: ${serverLib.getGameProfile(uuid)}")
                    }
                }

                literalCommand("name") {
                    val playerName by argument("name", StringArgumentType.word())

                    executes {
                        source.sendFeedback("Game profile: ${serverLib.getGameProfile(playerName)}")
                    }
                }
            }
        )

        registry.register(
            literalCommand("brigadier-nested-custom-type") {
                val target by argument("target", NestedGameProfilesArgumentType())

                executes {
                    val message =
                        when (target) {
                            is NestedGameProfilesTarget.Everyone -> "everyone"
                            is NestedGameProfilesTarget.Selector -> "native selector"
                        }

                    source.source.sendMessage("Nested custom type: $message")
                }
            }
        )

        registry.register(
            literalCommand("brigadier-position-selector") {
                val position by argument("position", McArgumentTypes.position())

                executes {
                    val resolved = position.resolve(source)

                    source.source.sendMessage("Position: $resolved; Source: ${source.source}; Executor: ${source.executor}")
                }
            }
        )

        registry.register(
            literalCommand("brigadier-block-position-selector") {
                val position by argument("position", McArgumentTypes.blockPosition())

                executes {
                    val resolved = position.resolve(source)

                    source.source.sendMessage("Block position: $resolved; Source: ${source.source}; Executor: ${source.executor}")
                }
            }
        )

        registry.register(
            literalCommand("brigadier-multi-arg") {
                val a by argument("a", IntegerArgumentType.integer())
                val b by argument("b", IntegerArgumentType.integer())

                executes {
                    source.source.sendMessage("Multi-arg: a=$a, b=$b")
                }
            }
        )

        registry.register(
            literalCommand("brigadier-intermediate-custom-arg") {
                val position by argument("position", McArgumentTypes.position())
                val message by argument("message", StringArgumentType.string())

                executes {
                    val resolved = position.resolve(source)

                    source.source.sendMessage("Position: $resolved; Message: $message")
                }
            }
        )

        // mcdsl refuses to mix arguments with sub-commands
        registry.register(
            LiteralArgumentBuilder.literal<McBrigadierSource>("brigadier-literal-after-argument")
                .then(
                    RequiredArgumentBuilder.argument<McBrigadierSource, Int>("value", IntegerArgumentType.integer())
                        .then(
                            LiteralArgumentBuilder.literal<McBrigadierSource>("double")
                                .executes { context ->
                                    val value = IntegerArgumentType.getInteger(context, "value")

                                    context.source.source.sendMessage("Literal after argument: ${value * 2}")

                                    Command.SINGLE_SUCCESS
                                }
                        )
                )
                .build()
        )

        val redirectCommand = literalCommand<McBrigadierSource>("brigadier-redirect") {
            val value by argument("value", IntegerArgumentType.integer())

            executes {
                source.source.sendMessage("Redirect: $value")
            }
        }
        redirectCommand.addChild(
            LiteralArgumentBuilder.literal<McBrigadierSource>("again")
                .redirect(redirectCommand)
                .build()
        )
        redirectCommand.addChild(
            LiteralArgumentBuilder.literal<McBrigadierSource>("fork")
                .fork(redirectCommand) { context -> listOf(context.source) }
                .build()
                .apply { addChild(LiteralArgumentBuilder.literal<McBrigadierSource>("shadowed").build()) }
        )
        registry.register(redirectCommand)

        registry.register(
            literalCommand("brigadier-unbound-requires") {
                requires { source ->
                    check(source.isBound) { "requires ran before the server was bound" }
                    true
                }

                executes {
                    source.sendFeedback("Unbound requires guard survived parsing")
                }
            }
        )

        registry.register(
            literalCommand("brigadier-denied-requires") {
                literalCommand("literal") {
                    requires {
                        logger.info("Denied requires checked: literal")
                        false
                    }

                    executes {
                        source.sendFeedback("Denied requires bypassed: literal")
                    }
                }

                literalCommand("argument") {
                    val value by argument("value", IntegerArgumentType.integer()) {
                        requires {
                            logger.info("Denied requires checked: argument")
                            false
                        }
                    }

                    executes {
                        source.sendFeedback("Denied requires bypassed: argument $value")
                    }
                }
            }
        )

        registry.register(
            literalCommand("brigadier-translated-feedback") {
                executes {
                    source.sendFeedback(
                        McTextComponent.translatable(
                            FEEDBACK_COMMAND_KEY,
                            McTextComponent.literal("translated"),
                        )
                    )
                }
            }
        )

        registry.register(
            literalCommand("brigadier-silent-feedback") {
                executes {
                    logger.info("Silent check: silent=${source.isSilent}")

                    source.sendFeedback("Silent check feedback")
                }
            }
        )
    }
}
