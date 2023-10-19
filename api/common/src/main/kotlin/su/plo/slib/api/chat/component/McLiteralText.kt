package su.plo.slib.api.chat.component

/**
 * Represents a Minecraft literal text component.
 *
 * @param text The plain text content of the text component.
 */
class McLiteralText(
    val text: String
) : McTextComponent() {

    override fun toString(): String {
        return text
    }
}
