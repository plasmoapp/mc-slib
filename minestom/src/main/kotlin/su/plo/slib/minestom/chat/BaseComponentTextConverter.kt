package su.plo.slib.minestom.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.component.McTranslatableText
import su.plo.slib.api.chat.converter.ServerTextConverter
import su.plo.slib.api.chat.style.McTextClickEvent
import su.plo.slib.api.chat.style.McTextHoverEvent
import su.plo.slib.api.language.ServerTranslator

class BaseComponentTextConverter(
    serverTranslator: ServerTranslator
) : ServerTextConverter<Component>(serverTranslator) {

    override fun convertToJson(text: Component): String =
            JSONComponentSerializer.json().serialize(text)

    override fun convertFromJson(json: String): Component =
            JSONComponentSerializer.json().deserialize(json)

    override fun convert(text: McTextComponent): Component {
        var component =
            if (text is McTranslatableText) convertTranslatable(text)
            else Component.text(text.toString())

        // apply styles
        component = applyStyles(component, text)

        // apply click event
        component = applyClickEvent(component, text.clickEvent)

        // apply hover event
        component = applyHoverEvent(component, text.hoverEvent)

        // add siblings
        for (sibling in text.siblings) {
            component = component.append(convert(sibling))
        }

        return component
    }

    private fun convertTranslatable(text: McTranslatableText): Component {
        return Component.translatable(text.key, *text.args.map { argument ->
            if (argument is McTextComponent) convert(argument)
            else Component.text(argument.toString())
        }.toTypedArray())
    }

    private fun applyClickEvent(
            component: Component,
            clickEvent: McTextClickEvent?
    ): Component {
        if (clickEvent == null) return component
        return component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.valueOf(clickEvent.action.name), clickEvent.value))
    }

    private fun applyHoverEvent(
            component: Component,
            hoverEvent: McTextHoverEvent?
    ): Component {
        if (hoverEvent == null) return component

        if (hoverEvent.action == McTextHoverEvent.Action.SHOW_TEXT) {
            return component.hoverEvent(HoverEvent.hoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    convert(hoverEvent.value as McTextComponent)
            ))
        }
        return component
    }

    private fun applyStyles(
            component: Component,
            text: McTextComponent
    ): Component {
        var newComponent = component
        text.styles.forEach { style ->
            newComponent = newComponent.color(NamedTextColor.NAMES.value(style.name))
        }
        return newComponent
    }
}
