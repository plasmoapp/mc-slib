package su.plo.slib.bungee

import net.kyori.adventure.key.Key
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.TranslationStore
import net.md_5.bungee.api.plugin.Plugin
import su.plo.slib.proxy.TestProxy
import java.text.MessageFormat
import java.util.Locale

class TestBungeePlugin : Plugin() {
    private val testProxy = TestProxy()

    override fun onEnable() {
        registerVanillaTranslations()
        val minecraftServer = BungeeProxyLib(this)
    }

    // Bungee doesn't ship vanilla translations, so translation keys leak to the console as raw ids
    // Register the minimum set the smoke tests need
    private fun registerVanillaTranslations() {
        val store = TranslationStore.messageFormat(Key.key("slib", "test"))
        store.defaultLocale(Locale.US)
        store.register("command.context.parse_error", Locale.US, MessageFormat("{0} at position {1}: {2}"))
        store.register("argument.uuid.invalid", Locale.US, MessageFormat("Invalid UUID"))
        GlobalTranslator.translator().addSource(store)
    }
}
