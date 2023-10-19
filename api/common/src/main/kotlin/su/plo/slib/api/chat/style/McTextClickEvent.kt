package su.plo.slib.api.chat.style

/**
 * Represents a Minecraft text click event.
 *
 * This class defines actions that can be performed when a player clicks on a text component.
 * It specifies the action to take and the associated value, such as opening a URL, running a command, or suggesting a command.
 *
 * @param action The type of action to perform when the text is clicked.
 * @param value  The value associated with the action, such as a URL or command.
 */
class McTextClickEvent private constructor(
    val action: Action,
    val value: String
) {

    /**
     * Click event actions.
     */
    enum class Action {
        OPEN_URL,
        OPEN_FILE,
        RUN_COMMAND,
        SUGGEST_COMMAND,
        CHANGE_PAGE,
        COPY_TO_CLIPBOARD
    }

    companion object {

        /**
         * Creates a click event that opens the specified URL when clicked.
         *
         * @param url The URL to be opened.
         * @return A [McTextClickEvent] instance with the "OPEN_URL" action and the provided URL.
         */
        @JvmStatic
        fun openUrl(url: String): McTextClickEvent {
            return McTextClickEvent(Action.OPEN_URL, url)
        }

        /**
         * Creates a click event that runs the specified command when clicked.
         *
         * @param command The command to be executed.
         * @return A [McTextClickEvent] instance with the "RUN_COMMAND" action and the provided command.
         */
        @JvmStatic
        fun runCommand(command: String): McTextClickEvent {
            return McTextClickEvent(Action.RUN_COMMAND, command)
        }

        /**
         * Creates a click event that suggests the specified command when clicked.
         *
         * @param command The command to be suggested.
         * @return A [McTextClickEvent] instance with the "SUGGEST_COMMAND" action and the provided command.
         */
        @JvmStatic
        fun suggestCommand(command: String): McTextClickEvent {
            return McTextClickEvent(Action.SUGGEST_COMMAND, command)
        }

        /**
         * Creates a click event that changes the page or content when clicked.
         *
         * @param page The new page or content to display.
         * @return A [McTextClickEvent] instance with the "CHANGE_PAGE" action and the provided page.
         */
        @JvmStatic
        fun changePage(page: String): McTextClickEvent {
            return McTextClickEvent(Action.CHANGE_PAGE, page)
        }

        /**
         * Creates a click event that copies the specified text to the clipboard when clicked.
         *
         * @param text The text to be copied to the clipboard.
         * @return A [McTextClickEvent] instance with the "COPY_TO_CLIPBOARD" action and the provided text.
         */
        @JvmStatic
        fun copyToClipboard(text: String): McTextClickEvent {
            return McTextClickEvent(Action.COPY_TO_CLIPBOARD, text)
        }

        /**
         * Creates a custom click event with the specified action and value.
         *
         * @param action The type of action to perform when clicked.
         * @param value  The value associated with the action.
         * @return A [McTextClickEvent] instance with the provided action and value.
         */
        @JvmStatic
        fun clickEvent(action: Action, value: String): McTextClickEvent {
            return McTextClickEvent(action, value)
        }
    }
}
