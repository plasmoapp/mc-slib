package su.plo.slib.api.chat.component

import su.plo.slib.api.chat.style.McTextClickEvent
import su.plo.slib.api.chat.style.McTextHoverEvent
import su.plo.slib.api.chat.style.McTextStyle
import java.util.*

/**
 * Represents a Minecraft text component.
 *
 * This abstract class serves as the base for creating rich-text components used in Minecraft messages.
 * It provides methods for adding styles, handling click and hover events, and composing text components.
 */
abstract class McTextComponent {

    /** List of text styles applied to this component. */
    val styles: MutableList<McTextStyle> = ArrayList()

    /** List of sibling text components that are displayed alongside this component. */
    val siblings: MutableList<McTextComponent> = ArrayList()

    /** The click event associated with this text component, if any. */
    var clickEvent: McTextClickEvent? = null

    /** The hover event associated with this text component, if any. */
    var hoverEvent: McTextHoverEvent? = null

    /**
     * Appends one or more text components to the list of siblings.
     *
     * @param components The text components to append.
     * @return This text component for method chaining.
     */
    fun append(vararg components: McTextComponent): McTextComponent {
        Collections.addAll(siblings, *components)
        return this
    }

    /**
     * Appends a collection of text components to the list of siblings.
     *
     * @param components The collection of text components to append.
     * @return This text component for method chaining.
     */
    fun append(components: Collection<McTextComponent>): McTextComponent {
        siblings.addAll(components)
        return this
    }

    /**
     * Applies a style to this text component.
     *
     * @param style The text style to apply.
     * @return This text component for method chaining.
     */
    fun withStyle(style: McTextStyle): McTextComponent {
        styles.add(style)
        return this
    }

    /**
     * Applies one or more styles to this text component.
     *
     * @param styles The text styles to apply.
     * @return This text component for method chaining.
     */
    fun withStyle(vararg styles: McTextStyle): McTextComponent {
        this.styles.addAll(Arrays.asList(*styles))
        return this
    }

    /**
     * Sets the click event for this text component.
     *
     * @param clickEvent The click event to set.
     * @return This text component for method chaining.
     */
    fun clickEvent(clickEvent: McTextClickEvent?): McTextComponent {
        this.clickEvent = clickEvent
        return this
    }

    /**
     * Sets the hover event for this text component.
     *
     * @param hoverEvent The hover event to set.
     * @return This text component for method chaining.
     */
    fun hoverEvent(hoverEvent: McTextHoverEvent?): McTextComponent {
        this.hoverEvent = hoverEvent
        return this
    }

    /**
     * Merges this text component with another text component, combining their styles, siblings, and events.
     *
     * @param component The text component to merge with.
     * @return The merged text component.
     */
    fun mergeWith(component: McTextComponent): McTextComponent {
        return withStyle(*component.styles.toTypedArray())
            .append(component.siblings)
            .clickEvent(component.clickEvent)
            .hoverEvent(component.hoverEvent)
    }

    companion object {

        /**
         * Creates a new literal text component with the specified text.
         *
         * @param text The plain text content.
         * @return A [McLiteralText] instance with the given text.
         */
        @JvmStatic
        fun literal(text: String): McLiteralText {
            return McLiteralText(text)
        }

        /**
         * Creates a new translatable text component with the specified translation key and arguments.
         *
         * @param key   The translation key.
         * @param args  The arguments to replace placeholders in the translation.
         * @return A [McTranslatableText] instance with the given key and arguments.
         */
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun translatable(key: String, vararg args: Any): McTranslatableText {
            return McTranslatableText(key, args as Array<Any>)
        }

        /**
         * Creates an empty text component with no content.
         *
         * @return An empty [McLiteralText] instance.
         */
        @JvmStatic
        fun empty(): McTextComponent {
            return McLiteralText("")
        }
    }
}
