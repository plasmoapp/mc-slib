package su.plo.slib.integration

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.Translator
import su.plo.slib.language.AdventureServerTranslator
import su.plo.slib.util.fromJson
import su.plo.slib.util.toJson
import java.text.MessageFormat
import java.util.*

class AdventureTranslatorIntegration(
    private val serverTranslator: AdventureServerTranslator
) : Translator {

    init {
        @Suppress("DEPRECATION")
        try {
            GlobalTranslator.translator().addSource(this)
        } catch (_: NoSuchMethodException) {
            GlobalTranslator.get().addSource(this)
        } catch (_: NoSuchMethodError) {
            GlobalTranslator.get().addSource(this)
        }
    }

    override fun name(): Key =
        Key.key("plasmo", "slib/integration/translator")

    override fun translate(component: TranslatableComponent, locale: Locale): Component? {
        val language = serverTranslator.getLanguage(locale.toString())
        if (!language.containsKey(component.key())) return null

        try {
            val json = GsonComponentSerializer.gson().serialize(component)
            val translatedJson = serverTranslator.translate(fromJson(json), locale)?.toJson()
                ?: return null

            return GsonComponentSerializer.gson().deserialize(translatedJson)
        } catch (_: Exception) {
            return null
        }
    }

    override fun translate(key: String, locale: Locale): MessageFormat? {
        return null
    }
}
