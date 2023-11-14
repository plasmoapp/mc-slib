package su.plo.slib.api.language

/**
 * Manages server translations which used to translate translatable components on server.
 */
object ServerTranslator {

    /**
     * Default language used to translate components.
     */
    var defaultLanguage: String = "en_us"
        set(value) {
            languages.computeIfAbsent(value) { HashMap() }
            field = value
        }

    private val languages: MutableMap<String, MutableMap<String, String>> = hashMapOf(
        "en_us" to HashMap()
    )

    /**
     * Registers the translations for translatable components.
     */
    @Synchronized
    fun register(languageName: String, languageMap: Map<String, String>) {
        val language = languages.computeIfAbsent(languageName) {
            HashMap()
        }

        language.putAll(languageMap)
    }

    /**
     * Gets the language map.
     *
     * If the provided language doesn't exist, [defaultLanguage] will be used.
     *
     * @return The language map.
     */
    @Synchronized
    fun getLanguage(languageName: String): Map<String, String> =
        languages.getOrElse(languageName) {
            languages[defaultLanguage] ?: throw IllegalStateException("Default language doesn't exist")
        }
}
