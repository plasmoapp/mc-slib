package su.plo.slib.api.chat.converter

import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.style.McTextHoverEvent
import su.plo.slib.api.chat.component.McTranslatableText

abstract class TranslatableTextConverter<T> : McTextConverter<T> {

    protected open fun translate(
        language: Map<String, String>,
        hoverEvent: McTextHoverEvent?
    ): McTextHoverEvent? {
        if (hoverEvent == null) return null

        if (hoverEvent.action == McTextHoverEvent.Action.SHOW_TEXT &&
            hoverEvent.value is McTranslatableText
        ) {
            return McTextHoverEvent.showText(
                translate(
                    language,
                    hoverEvent.value
                )
            )
        }

        return hoverEvent
    }

    protected fun translate(
        language: Map<String, String>,
        translatable: McTranslatableText
    ): McTextComponent {
        return if (translatable.args.any { it is McTextComponent }) {
            McTextComponent.translatable(
                language[translatable.key]!!.replace("&", "§"),
                *translatable.args
            )
                .mergeWith(translatable)
                .hoverEvent(translate(language, translatable.hoverEvent))
        } else {
            McTextComponent.literal(
                String.format(
                    language[translatable.key]!!,
                    *translatable.args
                ).replace("&", "§")
            )
                .mergeWith(translatable)
                .hoverEvent(translate(language, translatable.hoverEvent))

        }
    }

    protected fun translateInner(
        language: Map<String, String>,
        text: McTextComponent
    ): McTextComponent {
        return if (text is McTranslatableText) {
            translateArguments(language, text)
        } else {
            text
        }.also { translateSiblings(language, it) }
    }

    private fun translateArguments(
        language: Map<String, String>,
        text: McTranslatableText
    ): McTranslatableText {
        for (index in text.args.indices) {
            val argument = text.args[index]

            if (argument !is McTextComponent)
                continue

            val translatedText = translateInner(language, argument)
            if (translatedText !is McTranslatableText ||
                !language.containsKey(translatedText.key)
            ) {
                text.args[index] = translatedText
                continue
            }

            text.args[index] = translate(language, translatedText)
        }

        return text
    }

    private fun translateSiblings(
        language: Map<String, String>,
        text: McTextComponent
    ): McTextComponent {
        for (index in text.siblings.indices) {
            val sibling = translateInner(
                language,
                text.siblings[index]
            )

            text.siblings[index] = if (sibling is McTranslatableText && language.containsKey(sibling.key)) {
                translate(language, sibling)
            } else {
                sibling
            }
        }
        return text
    }
}
