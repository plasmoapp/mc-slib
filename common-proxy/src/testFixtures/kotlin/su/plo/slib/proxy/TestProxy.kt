package su.plo.slib.proxy

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import dev.apehum.mcdsl.command.literalCommand
import net.kyori.adventure.key.Key
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.TranslationStore
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.command.brigadier.McTextMessage
import su.plo.slib.api.event.command.McBrigadierCommandsRegisterEvent
import su.plo.slib.api.event.player.McPlayerJoinEvent
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.proxy.command.McProxyCommand
import su.plo.slib.api.proxy.event.command.McProxyCommandsRegisterEvent
import su.plo.slib.proxy.command.TranslatedArgumentType
import su.plo.slib.proxy.command.UuidArgumentType
import java.text.MessageFormat
import java.util.Locale

private val commandFailed = DynamicCommandExceptionType { value ->
    McTextMessage.of(
        McTextComponent.translatable(FAILED_COMMAND_KEY, McTextComponent.literal(value.toString()))
    )
}

class TestProxy {
    private var logger = McLoggerFactory.createLogger("TestProxy")

    init {
        McPlayerJoinEvent.registerListener { player ->
            logger.info("Player ${player.name} joined the server")
        }

        McPlayerQuitEvent.registerListener { player ->
            logger.info("Player ${player.name} quit the server")
        }

        McProxyCommandsRegisterEvent.registerListener { commands, proxy ->
            proxy.serverTranslator.defaultLanguage = UNTRANSLATED_LANGUAGE
            proxy.serverTranslator.register(TEST_LANGUAGE, testTranslations)

            commands.logRegisteredCommands = true

            commands.register("ping", object : McProxyCommand {
                override fun execute(
                    source: McCommandSource,
                    arguments: Array<String>,
                ) {
                    source.sendMessage("Pong")
                }
            })
        }

        McBrigadierCommandsRegisterEvent.registerListener { registry ->
            registry.register(
                literalCommand<McBrigadierSource>("brigadier-ping") {
                    executes {
                        source.source.sendMessage("Pong")
                    }
                }
            )

            registry.register(
                literalCommand<McBrigadierSource>("brigadier-custom-type") {
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
