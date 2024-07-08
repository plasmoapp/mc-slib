package su.plo.slib.api.language

/**
 * Format used in server languages.
 */
enum class ServerLanguageFormat {

    /**
     * Format with color codes starting with "&".
     */
    LEGACY_AMPERSAND,

    /**
     * [MiniMessage format](https://docs.advntr.dev/minimessage/format.html).
     */
    MINI_MESSAGE
}
