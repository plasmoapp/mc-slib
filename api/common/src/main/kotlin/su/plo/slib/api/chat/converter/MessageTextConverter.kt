package su.plo.slib.api.chat.converter

import com.mojang.brigadier.Message
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.service.lazyService

/**
 * Converts a [McTextComponent] into a platform-specific [Message] suitable for use with brigadier
 * (e.g. as the message inside a `CommandSyntaxException`).
 *
 * Implementations preserve literal and translatable components where the platform allows — typically
 * either by producing a native [Message] implementation (Vanilla `Component`, Velocity's
 * `VelocityBrigadierMessage`) or by wrapping the component in a platform-specific [Message] that
 * the command error-handling path recognizes and unwraps.
 */
interface MessageTextConverter {

    /**
     * Converts a [McTextComponent] into a brigadier [Message].
     *
     * @param text The [McTextComponent] to convert.
     * @return A [Message] representing the converted text component.
     */
    fun convert(text: McTextComponent): Message

    companion object {
        private val provider: MessageTextConverter by lazyService()

        @JvmStatic
        fun converter(): MessageTextConverter =
            provider
    }
}
