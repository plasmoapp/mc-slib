package su.plo.slib.api.chat.style

import su.plo.slib.api.chat.component.McTextComponent

/**
 * Represents a Minecraft text hover event.
 *
 * This class defines actions that can be performed when a player hovers over a text component.
 * It specifies the action to take and the associated value, such as displaying text information when hovered.
 *
 * @param action The type of action to perform when the text is hovered.
 * @param value  The value associated with the action, which can be text information or other data.
 */
class McTextHoverEvent private constructor(
    val action: Action,
    val value: Any
) {

    /**
     * Hover event actions.
     */
    enum class Action {
        SHOW_TEXT
        //        SHOW_ITEM,
        //        SHOW_ENTITY
    }

    companion object {

        /**
         * Creates a hover event that displays the specified text when hovered.
         *
         * @param text The [McTextComponent] to be displayed when hovered.
         * @return A [McTextHoverEvent] instance with the "SHOW_TEXT" action and the provided text component.
         */
        @JvmStatic
        fun showText(text: McTextComponent): McTextHoverEvent {
            return McTextHoverEvent(Action.SHOW_TEXT, text)
        }
    }
}
