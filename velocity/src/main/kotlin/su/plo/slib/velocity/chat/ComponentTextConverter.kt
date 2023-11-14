package su.plo.slib.velocity.chat

import com.google.common.collect.Lists
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.component.McTranslatableText
import su.plo.slib.api.chat.converter.ServerTextConverter
import su.plo.slib.api.chat.style.McTextClickEvent
import su.plo.slib.api.chat.style.McTextHoverEvent
import su.plo.slib.api.chat.style.McTextStyle

class ComponentTextConverter : ServerTextConverter<Component>() {

    override fun convertToJson(text: Component) =
        GsonComponentSerializer.gson().serialize(text)

    override fun convertFromJson(json: String) =
        GsonComponentSerializer.gson().deserialize(json)

    override fun convert(text: McTextComponent): Component {
        var component: Component

        component =
            if (text is McTranslatableText) convertTranslatable(text)
            else Component.text(text.toString())

        // apply styles
        component = component.style(getStyles(text))

        // apply click event
        component = applyClickEvent(component, text.clickEvent)

        // apply hover event
        component = applyHoverEvent(component, text.hoverEvent)

        // add siblings
        text.siblings.forEach {
            component = component.append(convert(it))
        }

        return component
    }

    private fun convertTranslatable(text: McTranslatableText): Component {
        val args: MutableList<Component> = Lists.newArrayList()

        for (i in 0 until text.args.size) {
            val arg = text.args[i]
            if (arg is McTextComponent) {
                args.add(convert(arg))
            } else {
                args.add(Component.text(arg.toString()))
            }
        }

        return Component.translatable(text.key, args)
    }

    private fun applyClickEvent(
        component: Component,
        clickEvent: McTextClickEvent?
    ): Component =
        if (clickEvent == null) component
        else component.clickEvent(
            ClickEvent.clickEvent(
                ClickEvent.Action.valueOf(clickEvent.action.name),
                clickEvent.value
            )
        )

    private fun applyHoverEvent(
        component: Component,
        hoverEvent: McTextHoverEvent?
    ): Component {
        if (hoverEvent == null) return component

        // todo: waytoodank
        return if (hoverEvent.action === McTextHoverEvent.Action.SHOW_TEXT) {
            component.hoverEvent(HoverEvent.showText(convert(hoverEvent.value as McTextComponent)))
        } else component
    }

    private fun getStyles(text: McTextComponent): Style {
        val builder = Style.style()
        text.styles.forEach { convertStyle(builder, it) }
        return builder.build()
    }

    private fun convertStyle(builder: Style.Builder, style: McTextStyle): Style.Builder {
        if (style.type === McTextStyle.Type.COLOR) {
            builder.color(NamedTextColor.NAMES.value(style.name))
        } else if (style.type === McTextStyle.Type.DECORATION) {
            builder.decoration(TextDecoration.NAMES.value(style.name)!!, true)
        }
        return builder
    }
}
