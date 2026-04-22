package su.plo.slib.minestom.chat

import com.mojang.brigadier.Message
import su.plo.slib.api.chat.component.McTextComponent

class McTextMessage(
    val component: McTextComponent,
) : Message {
    override fun getString(): String = component.toString()
}
