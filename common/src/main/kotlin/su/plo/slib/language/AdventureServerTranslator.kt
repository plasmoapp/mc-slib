package su.plo.slib.language

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.Translator
import su.plo.slib.api.language.ServerLanguageFormat
import su.plo.slib.api.language.ServerTranslator
import java.text.MessageFormat
import java.util.*

class AdventureServerTranslator : Translator, ServerTranslator {

    init {
        GlobalTranslator.translator().addSource(this)
    }

    private val serverTranslator = MapServerTranslator()

    override var defaultLanguage: String
        get() = serverTranslator.defaultLanguage
        set(value) {
            serverTranslator.defaultLanguage = value
        }

    override var format: ServerLanguageFormat
        get() = serverTranslator.format
        set(value) {
            serverTranslator.format = value
        }

    override fun register(languageName: String, languageMap: Map<String, String>) {
        serverTranslator.register(languageName, languageMap)
    }

    override fun getLanguage(languageName: String): Map<String, String> =
        serverTranslator.getLanguage(languageName)

    override fun name(): Key =
        Key.key("plasmo", "slib/translator")

    override fun translate(component: TranslatableComponent, locale: Locale): Component? {
        val language = getLanguage(locale.toString())
        val translationString = language[component.key()] ?: return null

        return when (format) {
            ServerLanguageFormat.LEGACY_AMPERSAND ->
                LegacyComponentRenderer.renderTranslatable(component, translationString, locale)

            ServerLanguageFormat.MINI_MESSAGE ->
                MiniMessageComponentRenderer.renderTranslatable(component, translationString, locale)
        }
    }

    override fun translate(key: String, locale: Locale): MessageFormat? {
        return null
    }
}
