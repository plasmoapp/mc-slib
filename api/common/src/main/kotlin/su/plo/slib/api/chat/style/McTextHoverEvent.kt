package su.plo.slib.api.chat.style

import su.plo.slib.api.chat.component.McTextComponent

class McTextHoverEvent private constructor(
    val action: Action,
    val value: Any
) {

    enum class Action {
        SHOW_TEXT
        //        SHOW_ITEM,
        //        SHOW_ENTITY
    }

    companion object {
        fun showText(text: McTextComponent): McTextHoverEvent {
            return McTextHoverEvent(Action.SHOW_TEXT, text)
        }
    }
}
