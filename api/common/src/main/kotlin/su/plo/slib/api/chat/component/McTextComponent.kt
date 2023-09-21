package su.plo.slib.api.chat.component

import su.plo.slib.api.chat.style.McTextClickEvent
import su.plo.slib.api.chat.style.McTextHoverEvent
import su.plo.slib.api.chat.style.McTextStyle
import java.util.*

abstract class McTextComponent {

    val styles: MutableList<McTextStyle> = ArrayList()
    val siblings: MutableList<McTextComponent> = ArrayList()

    var clickEvent: McTextClickEvent? = null
    var hoverEvent: McTextHoverEvent? = null

    fun append(vararg components: McTextComponent): McTextComponent {
        Collections.addAll(siblings, *components)
        return this
    }

    fun append(components: Collection<McTextComponent>): McTextComponent {
        siblings.addAll(components)
        return this
    }

    fun withStyle(style: McTextStyle): McTextComponent {
        styles.add(style)
        return this
    }

    fun withStyle(vararg styles: McTextStyle): McTextComponent {
        this.styles.addAll(Arrays.asList(*styles))
        return this
    }

    fun clickEvent(clickEvent: McTextClickEvent?): McTextComponent {
        this.clickEvent = clickEvent
        return this
    }

    fun hoverEvent(hoverEvent: McTextHoverEvent?): McTextComponent {
        this.hoverEvent = hoverEvent
        return this
    }

    fun mergeWith(component: McTextComponent): McTextComponent {
        return withStyle(*component.styles.toTypedArray())
            .append(component.siblings)
            .clickEvent(component.clickEvent)
            .hoverEvent(component.hoverEvent)
    }

    companion object {
        fun literal(text: String): McLiteralText {
            return McLiteralText(text)
        }

        @Suppress("UNCHECKED_CAST")
        fun translatable(key: String, vararg args: Any): McTranslatableText {
            return McTranslatableText(key, args as Array<Any>)
        }

        fun empty(): McTextComponent {
            return McLiteralText("")
        }
    }
}
