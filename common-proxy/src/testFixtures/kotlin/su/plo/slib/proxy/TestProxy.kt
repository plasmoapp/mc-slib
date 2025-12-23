package su.plo.slib.proxy

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.event.player.McPlayerJoinEvent
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.proxy.command.McProxyCommand
import su.plo.slib.api.proxy.event.command.McProxyCommandsRegisterEvent

class TestProxy {
    private var logger = McLoggerFactory.createLogger("TestProxy")

    init {
        McPlayerJoinEvent.registerListener { player ->
            logger.info("Player ${player.name} joined the server")
        }

        McPlayerQuitEvent.registerListener { player ->
            logger.info("Player ${player.name} quit the server")
        }

        McProxyCommandsRegisterEvent.registerListener { commands, minecraftProxy ->
            commands.register("ping", object : McProxyCommand {
                override fun execute(
                    source: McCommandSource,
                    arguments: Array<String>,
                ) {
                    source.sendMessage("Pong")
                }
            })

            commands.register(
                LiteralArgumentBuilder.literal<Any>("brigadier-ping")
                    .executes {
                        val context = commands.getBrigadierContext(it)
                        context.source.sendMessage("Pong")

                        Command.SINGLE_SUCCESS
                    }
            )
        }
    }
}
