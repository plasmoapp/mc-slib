package su.plo.slib.language

import su.plo.slib.api.language.ServerTranslator

class MapServerTranslator : ServerTranslator {

    override var defaultLanguage: String = "en_us"
        set(value) {
            languages.computeIfAbsent(value) { HashMap() }
            field = value
        }

    private val languages: MutableMap<String, MutableMap<String, String>> = hashMapOf(
        "en_us" to HashMap()
    )

    @Synchronized
    override fun register(languageName: String, languageMap: Map<String, String>) {
        val language = languages.computeIfAbsent(languageName) {
            HashMap()
        }

        language.putAll(languageMap)
    }

    @Synchronized
    override fun getLanguage(languageName: String): Map<String, String> =
        languages.getOrElse(languageName) {
            languages[defaultLanguage] ?: throw IllegalStateException("Default language doesn't exist")
        }
}
