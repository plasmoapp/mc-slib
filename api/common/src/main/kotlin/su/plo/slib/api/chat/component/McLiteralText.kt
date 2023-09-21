package su.plo.slib.api.chat.component

/**
 * Text component contains plain text
 */
class McLiteralText(
    val text: String
) : McTextComponent() {

    override fun toString(): String {
        return text
    }
}
