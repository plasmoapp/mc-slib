package su.plo.slib.language

import net.kyori.adventure.key.Key
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.Translator
import su.plo.slib.api.language.ServerTranslator
import java.text.MessageFormat
import java.util.*

class AdventureServerTranslator : ServerTranslator, Translator {

    init {
        try {
            GlobalTranslator.translator().addSource(this)
        } catch (_: NoSuchMethodError) {
            GlobalTranslator.get().addSource(this)
        }
    }

    private val serverTranslator = MapServerTranslator()

    override var defaultLanguage: String
        get() = serverTranslator.defaultLanguage
        set(value) {
            serverTranslator.defaultLanguage = value
        }

    override fun register(languageName: String, languageMap: Map<String, String>) {
        serverTranslator.register(languageName, languageMap)
    }

    override fun getLanguage(languageName: String): Map<String, String> =
        serverTranslator.getLanguage(languageName)

    override fun name(): Key =
        Key.key("plasmo", "voice/v2/translator")

    override fun translate(key: String, locale: Locale): MessageFormat? {
        val language = getLanguage(locale.toString())

        return language[key]?.let { MessageFormat(it) }
    }
}
