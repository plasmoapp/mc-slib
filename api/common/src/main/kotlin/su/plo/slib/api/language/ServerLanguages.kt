package su.plo.slib.api.language

import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.component.McTranslatableText
import su.plo.slib.api.command.McChatHolder
import su.plo.slib.api.resource.ResourceLoader
import java.io.File
import java.util.concurrent.CompletableFuture

interface ServerLanguages {

    /**
     * Registers a new language using resources
     *
     * Reads **languages/list** from resources using [ResourceLoader]
     *
     * In the **list** you should specify list of languages separated by **\n**
     *
     * After reading the list, all languages from it will be read from resources **languages/?.toml**
     * and saved to **languageFolder/?.toml**
     *
     *
     * You can edit and create new languages in languagesFolder, they will not be overwritten or deleted
     *
     * Default language is **en_us**, can be changed in server config
     */
    fun register(
        resourceLoader: ResourceLoader,
        languagesFolder: File
    ): CompletableFuture<Void>

    /**
     * Registers a new language using crowdin
     *
     * Works as [register], but also uses crowdin as default languages source
     *
     * Crowdin translations will be cached in **languageFolder/.crowdin** for **3 days**
     *
     * Default language is **en_us**, can be changed in server config
     */
    fun register(
        crowdinProjectId: String,
        fileName: String?,
        resourceLoader: ResourceLoader,
        languagesFolder: File
    ): CompletableFuture<Void>

    /**
     * Gets server language by name or default language if not found
     */
    fun getServerLanguage(languageName: String?): Map<String, String>

    /**
     * Gets client language by name or default language if not found
     */
    fun getClientLanguage(languageName: String?): Map<String, String>

    /**
     * Gets default server language
     */
    val serverLanguage: Map<String, String>
        get() = getServerLanguage(null)

    /**
     * Gets server language by chat holder
     */
    fun getServerLanguage(holder: McChatHolder): Map<String, String> {
        return getServerLanguage(holder.language)
    }

    /**
     * Gets default client language
     */
    val clientLanguage: Map<String, String>
        get() = getClientLanguage(null)

    /**
     * Gets client language by chat holder
     */
    fun getClientLanguage(holder: McChatHolder): Map<String, String> {
        return getClientLanguage(holder.language)
    }

    /**
     * Translates text using server language
     */
    fun translate(
        text: McTranslatableText,
        holder: McChatHolder,
        key: String
    ): McTextComponent? {
        val language = getServerLanguage(holder.language)
        return if (!language.containsKey(key)) text else McTextComponent.literal(language[key]!!)
    }
}
