package su.plo.slib.api.command

import su.plo.slib.api.chat.component.McTextComponent

/**
 * Represents a receiver of Minecraft messages, capable of sending various types of chat messages.
 */
interface McChatHolder {

    /**
     * Gets the current language of the chat holder.
     */
    val language: String

    /**
     * Sends a chat message to the chat holder.
     *
     * @param text The chat component to send.
     */
    fun sendMessage(text: McTextComponent)

    /**
     * Sends a chat message to the chat holder.
     *
     * @param text The string message to send.
     */
    fun sendMessage(text: String) {
        sendMessage(McTextComponent.literal(text))
    }

    /**
     * Sends an action bar message to the chat holder.
     *
     * If the [McChatHolder] does not support the action bar, the message will be sent to the chat instead.
     *
     * @param text The chat component to send as an action bar message.
     */
    fun sendActionBar(text: McTextComponent)

    /**
     * Sends an action bar message to the chat holder.
     *
     * If the [McChatHolder] does not support the action bar, the message will be sent to the chat instead.
     *
     * @param text The string message to send as an action bar message.
     */
    fun sendActionBar(text: String) {
        sendActionBar(McTextComponent.literal(text))
    }
}
