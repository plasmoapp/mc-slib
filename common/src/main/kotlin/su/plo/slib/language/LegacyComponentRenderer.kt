package su.plo.slib.language

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.translation.GlobalTranslator
import java.util.*

object LegacyComponentRenderer {

    fun renderTranslatable(
        component: TranslatableComponent,
        legacyString: String,
        locale: Locale,
        renderer: TranslatableComponentRenderer<Locale> = GlobalTranslator.renderer()
    ): Component =
        LegacyComponentSerializer.legacyAmpersand().deserialize(legacyString)
            .mergeStyle(component)
            .hoverEvent(
                component.hoverEvent()?.withRenderedValue(renderer, locale)
            )
            .children(
                component.children().map { renderer.render(it, locale) }
            )
            .let { translation ->
                var index = 0

                translation.replaceText(
                    TextReplacementConfig.builder()
                        .match("%(\\d+\\$)?[\\d.]*[a-zA-Z]")
                        .replacement { matchResult, builder ->
                            val argumentIndex = matchResult.group(1)
                                ?.dropLast(1)
                                ?.toIntOrNull()
                                ?.let { it - 1 }
                                ?: index++

                            val argument = component.args()[argumentIndex]
                                ?: return@replacement builder

                            renderer.render(argument, locale)
                        }
                        .build()
                )
            }
}