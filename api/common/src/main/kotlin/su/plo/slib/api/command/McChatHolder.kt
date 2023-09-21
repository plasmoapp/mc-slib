package su.plo.slib.api.command

import su.plo.slib.api.chat.component.McTextComponent

/**
 * A receiver of any kind of Minecraft messages
 */
interface McChatHolder {

    /**
     * Gets the current language
     */
    val language: String

    /**
     * Sends the chat message
     *
     * @param text chat component to send
     */
    fun sendMessage(text: McTextComponent)

    /**
     * Sends the chat message
     *
     * @param text string to send
     */
    fun sendMessage(text: String) {
        sendMessage(McTextComponent.literal(text))
    }

    /**
     * Sends the action bar message
     *
     * If [McChatHolder] doesn't support action bar, message will be sent to chat instead
     *
     * @param text chat component to send
     */
    fun sendActionBar(text: McTextComponent)

    /**
     * Sends the action bar message
     *
     * If [McChatHolder] doesn't support action bar, message will be sent to chat instead
     *
     * @param text string to send
     */
    fun sendActionBar(text: String) {
        sendActionBar(McTextComponent.literal(text))
    }
}
