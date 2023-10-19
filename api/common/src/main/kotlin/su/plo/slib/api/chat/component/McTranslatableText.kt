package su.plo.slib.api.chat.component

/**
 * Represents a Minecraft translatable text component.
 *
 * This class represents a text component that can be translated based on a localization key and optional arguments.
 *
 * @param key  The translation key used to look up the localized text.
 * @param args An array of arguments to replace placeholders in the localized text.
 */
class McTranslatableText(
    val key: String,
    val args: Array<Any>
) : McTextComponent() {

    override fun toString(): String {
        return key
    }
}
