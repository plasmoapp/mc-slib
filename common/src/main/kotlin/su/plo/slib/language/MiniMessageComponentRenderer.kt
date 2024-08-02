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
        MiniMessage.miniMessage().deserialize(
            miniMessageString
                .let { convertLegacy(it) }
                .let { convertArgsToTags(it) },
            ArgumentTagResolver(component.args(), locale, renderer)
        )
            .mergeStyle(component)
            .hoverEvent(
                component.hoverEvent()?.withRenderedValue(renderer, locale)
            )
            .let { c ->
                c.children(
                    c.children() + component.children().map { renderer.render(it, locale) }
                )
            }
            .let { renderer.render(it, locale) }

    private fun convertLegacy(miniMessageString: String, prefix: String = "&"): String =
        miniMessageString
            .replace("${prefix}0", "<black>")
            .replace("${prefix}1", "<dark_blue>")
            .replace("${prefix}2", "<dark_green>")
            .replace("${prefix}3", "<dark_aqua>")
            .replace("${prefix}4", "<dark_red>")
            .replace("${prefix}5", "<dark_purple>")
            .replace("${prefix}6", "<gold>")
            .replace("${prefix}7", "<gray>")
            .replace("${prefix}8", "<dark_gray>")
            .replace("${prefix}9", "<blue>")
            .replace("${prefix}a", "<green>")
            .replace("${prefix}b", "<aqua>")
            .replace("${prefix}c", "<red>")
            .replace("${prefix}d", "<light_purple>")
            .replace("${prefix}e", "<yellow>")
            .replace("${prefix}f", "<white>")

            .replace("${prefix}n", "<underlined>")
            .replace("${prefix}m", "<strikethrough>")
            .replace("${prefix}k", "<obfuscated>")
            .replace("${prefix}o", "<italic>")
            .replace("${prefix}l", "<bold>")
            .replace("${prefix}r", "<reset>")

            .replace(Regex("${prefix}#([0-9a-fA-F]{6})")) {
                "<#${it.groupValues[1]}>"
            }

    private val stringFormatRegex = Regex("%(\\d+\\$)?[\\d.]*[a-zA-Z]")

    private fun convertArgsToTags(miniMessageString: String): String {
        var index = 0

        return miniMessageString.replace(stringFormatRegex) { match ->
            val position = match.groupValues[1]
                .takeIf { it.isNotEmpty() }
                ?.dropLast(1)
                ?.toIntOrNull()
                ?.minus(1)
                ?: index++

            "<argument:$position>"
        }
    }

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