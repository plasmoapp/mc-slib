package su.plo.slib.api.command.brigadier

import com.mojang.brigadier.Message
import su.plo.slib.api.chat.component.McTextComponent

/**
 * A brigadier [Message] wrapper for [McTextComponent].
 *
 * This wrapper only works with commands registered with [McBrigadierRegistry].
 * If you try to pass it to native brigadier instance, it'll be plain [getString].
 */
class McTextMessage private constructor(
    val component: McTextComponent,
) : Message {
    override fun getString(): String = component.toString()

    companion object {
        /**
         * Creates a [Message].
         *
         * @param component The text component.
         * @return A [McTextMessage] wrapping the given component.
         */
        @JvmStatic
        fun of(component: McTextComponent): McTextMessage =
            McTextMessage(component)
    }
}
