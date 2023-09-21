package su.plo.slib.api.chat.component

class McTranslatableText(
    val key: String,
    val args: Array<Any>
) : McTextComponent() {

    override fun toString(): String {
        return key
    }
}
