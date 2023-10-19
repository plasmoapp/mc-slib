package su.plo.slib.mod.chat

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.*
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.component.McTranslatableText
import su.plo.slib.api.chat.converter.McTextConverter
import su.plo.slib.api.chat.style.McTextClickEvent
import su.plo.slib.api.chat.style.McTextHoverEvent
import su.plo.slib.api.chat.style.McTextStyle

class ComponentTextConverter : McTextConverter<Component> {

    // todo: legacy
    override fun convertToJson(text: Component): String {
        return Component.Serializer.toJson(text)
    }

    // todo: legacy
    override fun convertFromJson(json: String): Component {
        return Component.Serializer.fromJson(json)!!
    }

    override fun convert(text: McTextComponent): Component {
        var component =
            if (text is McTranslatableText)
                convertTranslatable(text)
            else
                //#if MC>=11900
                Component.literal(text.toString())
                //#else
                //$$ TextComponent(text.toString())
                //#endif

        // apply styles
        component = applyStyles(component, text.styles)

        // apply click event
        component = applyClickEvent(component, text.clickEvent)

        // apply hover event
        component = applyHoverEvent(component, text.hoverEvent)

        // add siblings
        for (sibling in text.siblings) {
            component.append(convert(sibling))
        }
        return component
    }

    private fun convertTranslatable(text: McTranslatableText): MutableComponent {
        val args = arrayOfNulls<Any>(text.args.size)
        for (i in args.indices) {
            val arg = text.args[i]
            if (arg is McTextComponent) {
                args[i] = convert(arg)
            } else {
                args[i] = arg
            }
        }

        //#if MC>=11900
        return Component.translatable(text.key, *args)
        //#else
        //$$ return TranslatableComponent(text.key, *args)
        //#endif
    }

    private fun applyClickEvent(
        component: MutableComponent,
        clickEvent: McTextClickEvent?
    ): MutableComponent {
        if (clickEvent == null) return component

        component.setStyle(
            component.style.withClickEvent(
                ClickEvent(
                    ClickEvent.Action.valueOf(clickEvent.action.name),
                    clickEvent.value
                )
            )
        )

        return component
    }

    private fun applyHoverEvent(
        component: MutableComponent,
        hoverEvent: McTextHoverEvent?
    ): MutableComponent {
        if (hoverEvent == null) return component

        // todo: waytoodank
        if (hoverEvent.action === McTextHoverEvent.Action.SHOW_TEXT) {
            component.setStyle(
                component.style.withHoverEvent(
                    HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        convert(hoverEvent.value as McTextComponent)
                    )
                )
            )
        }

        return component
    }

    private fun applyStyles(
        component: MutableComponent,
        styles: List<McTextStyle>
    ): MutableComponent {
        if (styles.isEmpty()) return component

        component.setStyle(
            component.style.applyFormats(
                *styles.map { convertStyle(it) }.toTypedArray()
            )
        )

        return component
    }

    private fun convertStyle(style: McTextStyle) =
        ChatFormatting.valueOf(style.name)
}
