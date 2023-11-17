@file:Suppress("deprecation")
package su.plo.slib.spigot.chat

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.*
import net.md_5.bungee.api.chat.hover.content.Text
import net.md_5.bungee.chat.ComponentSerializer
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.component.McTranslatableText
import su.plo.slib.api.chat.converter.ServerTextConverter
import su.plo.slib.api.chat.style.McTextClickEvent
import su.plo.slib.api.chat.style.McTextHoverEvent
import su.plo.slib.api.language.ServerTranslator

class BaseComponentTextConverter(
    serverTranslator: ServerTranslator
) : ServerTextConverter<BaseComponent>(serverTranslator) {

    override fun convertToJson(text: BaseComponent): String =
        ComponentSerializer.toString(text)

    override fun convertFromJson(json: String): BaseComponent =
        ComponentSerializer.parse(json)[0]

    override fun convert(text: McTextComponent): BaseComponent {
        val component =
            if (text is McTranslatableText) convertTranslatable(text)
            else TextComponent(text.toString())

        // apply styles
        applyStyles(component, text)

        // apply click event
        applyClickEvent(component, text.clickEvent)

        // apply hover event
        applyHoverEvent(component, text.hoverEvent)

        // add siblings
        for (sibling in text.siblings) {
            component.addExtra(convert(sibling))
        }

        return component
    }

    private fun convertTranslatable(text: McTranslatableText): BaseComponent {
        return TranslatableComponent(
            text.key,
            *text.args.map { argument ->
                if (argument is McTextComponent) convert(argument)
                else argument
            }.toTypedArray()
        )
    }

    private fun applyClickEvent(
        component: BaseComponent,
        clickEvent: McTextClickEvent?
    ) {
        if (clickEvent == null) return

        component.clickEvent = ClickEvent(
            ClickEvent.Action.valueOf(clickEvent.action.name),
            clickEvent.value
        )
    }

    private fun applyHoverEvent(
        component: BaseComponent,
        hoverEvent: McTextHoverEvent?
    ) {
        if (hoverEvent == null) return

        if (hoverEvent.action == McTextHoverEvent.Action.SHOW_TEXT) {
            component.hoverEvent = HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                Text(ComponentBuilder(convert(hoverEvent.value as McTextComponent)).create())
            )
        }
    }

    private fun applyStyles(
        component: BaseComponent,
        text: McTextComponent
    ) {
        text.styles.forEach { style ->
            component.color = ChatColor.of(style.name)
        }
    }
}
