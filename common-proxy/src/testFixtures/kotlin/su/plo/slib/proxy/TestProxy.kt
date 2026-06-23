package su.plo.slib.proxy

import com.mojang.brigadier.Command
import net.kyori.adventure.key.Key
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.TranslationStore
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.event.player.McPlayerJoinEvent
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.proxy.command.McProxyCommand
import su.plo.slib.api.proxy.event.command.McProxyCommandsRegisterEvent
import su.plo.slib.proxy.command.UuidArgumentType
import java.text.MessageFormat
import java.util.Locale
import java.util.UUID

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
            commands.logRegisteredCommands = true

            commands.register("ping", object : McProxyCommand {
                override fun execute(
                    source: McCommandSource,
                    arguments: Array<String>,
                ) {
                    source.sendMessage("Pong")
                }
            })

            commands.register(
                McCommandManager.literal("brigadier-ping")
                    .executes {
                        val source = it.source.source
                        source.sendMessage("Pong")

                        Command.SINGLE_SUCCESS
                    }
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
        }

        registerVanillaTranslations()
    }

    // Bungee/Velocity doesn't ship vanilla translations, so translation keys leak to the console as raw ids
    // Register the minimum set the smoke tests need
    private fun registerVanillaTranslations() {
        val store = TranslationStore.messageFormat(Key.key("slib", "test"))
        store.defaultLocale(Locale.US)
        store.register("command.context.parse_error", Locale.US, MessageFormat("{0} at position {1}: {2}"))
        store.register("argument.uuid.invalid", Locale.US, MessageFormat("Invalid UUID"))
        GlobalTranslator.translator().addSource(store)
    }
}
