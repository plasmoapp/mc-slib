package su.plo.slib.integration

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.Translator
import su.plo.slib.api.language.ServerTranslator
import java.text.MessageFormat
import java.util.*

class AdventureTranslatorIntegration(
    private val serverTranslator: ServerTranslator
) : Translator {

    init {
        try {
            GlobalTranslator.translator().addSource(this)
        } catch (e: NoSuchMethodException) {
            GlobalTranslator.get().addSource(this)
        }
    }

    override fun name(): Key =
        Key.key("plasmo", "slib/integration/translator")

    override fun translate(component: TranslatableComponent, locale: Locale): Component? {
        val language = serverTranslator.getLanguage(locale.toString())
        val translationString = language[component.key()] ?: return null

        return LegacyComponentRenderer.renderTranslatable(component, translationString, locale)
    }

    override fun translate(key: String, locale: Locale): MessageFormat? {
        return null
    }
}