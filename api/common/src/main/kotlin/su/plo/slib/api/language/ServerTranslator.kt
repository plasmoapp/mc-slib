package su.plo.slib.api.language

/**
 * Manages server translations which used to translate translatable components on server.
 */
interface ServerTranslator {

    /**
     * Default language used to translate components.
     */
    var defaultLanguage: String

    /**
     * Format used to parse components.
     */
    var format: ServerLanguageFormat

    /**
     * Registers the translations for translatable components.
     */
    fun register(languageName: String, languageMap: Map<String, String>)

    /**
     * Gets the language map.
     *
     * If the provided language doesn't exist, [defaultLanguage] will be used.
     *
     * @return The language map.
     */
    fun getLanguage(languageName: String): Map<String, String>
}
