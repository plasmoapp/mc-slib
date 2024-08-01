package su.plo.slib.language

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer
import net.kyori.adventure.translation.GlobalTranslator
import java.util.*

object MiniMessageComponentRenderer {

    fun renderTranslatable(
        component: TranslatableComponent,
        miniMessageString: String,
        locale: Locale,
        renderer: TranslatableComponentRenderer<Locale> = GlobalTranslator.renderer()
    ): Component =
        MiniMessage.miniMessage().deserialize(miniMessageString, ArgumentTagResolver(component.args(), locale, renderer))
            .mergeStyle(component)
            .hoverEvent(
                component.hoverEvent()?.withRenderedValue(renderer, locale)
            )
            .children(
                component.children().map { renderer.render(it, locale) }
            )
            .let { renderer.render(it, locale) }

    class ArgumentTagResolver(
        private val arguments: List<Component>,
        private val locale: Locale,
        private val renderer: TranslatableComponentRenderer<Locale> = GlobalTranslator.renderer()
    ) : TagResolver {

        override fun resolve(name: String, arguments: ArgumentQueue, ctx: Context): Tag? {
            val argumentIndex = arguments.popOr("No argument number provided")
                .asInt()
                .orElseThrow { ctx.newException("Invalid argument number", arguments) }

            if (argumentIndex < 0 || argumentIndex >= this.arguments.size) {
                throw ctx.newException("Invalid argument number", arguments)
            }

            return this.arguments.getOrNull(argumentIndex)
                ?.let { renderer.render(it, locale) }
                ?.let { Tag.inserting(it) }
        }

        override fun has(name: String): Boolean =
            name == "argument"
    }
}