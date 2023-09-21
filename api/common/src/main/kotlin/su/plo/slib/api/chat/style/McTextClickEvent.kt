package su.plo.slib.api.chat.style

class McTextClickEvent private constructor(
    val action: Action,
    val value: String
) {

    enum class Action {
        OPEN_URL,
        OPEN_FILE,
        RUN_COMMAND,
        SUGGEST_COMMAND,
        CHANGE_PAGE,
        COPY_TO_CLIPBOARD
    }

    companion object {
        fun openUrl(url: String): McTextClickEvent {
            return McTextClickEvent(Action.OPEN_URL, url)
        }

        fun runCommand(command: String): McTextClickEvent {
            return McTextClickEvent(Action.RUN_COMMAND, command)
        }

        fun suggestCommand(command: String): McTextClickEvent {
            return McTextClickEvent(Action.SUGGEST_COMMAND, command)
        }

        fun changePage(page: String): McTextClickEvent {
            return McTextClickEvent(Action.CHANGE_PAGE, page)
        }

        fun copyToClipboard(text: String): McTextClickEvent {
            return McTextClickEvent(Action.COPY_TO_CLIPBOARD, text)
        }

        fun clickEvent(action: Action, value: String): McTextClickEvent {
            return McTextClickEvent(action, value)
        }
    }
}
