package su.plo.slib.api.chat.style

/**
 * Minecraft text styles and formatting options that can be applied to text components.
 *
 * @param type       The type of text style (color, decoration, or reset).
 * @param colorChar  The character representing the style in Minecraft text formatting codes.
 */
enum class McTextStyle(
    val type: Type,
    val colorChar: Char
) {

    // Color styles
    BLACK(Type.COLOR, '0'),
    DARK_BLUE(Type.COLOR, '1'),
    DARK_GREEN(Type.COLOR, '2'),
    DARK_AQUA(Type.COLOR, '3'),
    DARK_RED(Type.COLOR, '4'),
    DARK_PURPLE(Type.COLOR, '5'),
    GOLD(Type.COLOR, '6'),
    GRAY(Type.COLOR, '7'),
    DARK_GRAY(Type.COLOR, '8'),
    BLUE(Type.COLOR, '9'),
    GREEN(Type.COLOR, 'a'),
    AQUA(Type.COLOR, 'b'),
    RED(Type.COLOR, 'c'),
    LIGHT_PURPLE(Type.COLOR, 'd'),
    YELLOW(Type.COLOR, 'e'),
    WHITE(Type.COLOR, 'f'),

    // Decoration styles
    OBFUSCATED(Type.DECORATION, 'k'),
    BOLD(Type.DECORATION, 'l'),
    STRIKETHROUGH(Type.DECORATION, 'm'),
    UNDERLINE(Type.DECORATION, 'n'),
    ITALIC(Type.DECORATION, 'o'),

    // Reset style
    RESET(Type.RESET, 'r');

    /**
     * Converts the style to its legacy text formatting code representation.
     *
     * @return A string containing the legacy text formatting code for the style.
     */
    override fun toString(): String {
        return String.format("%s%s", COLOR_CHAR, colorChar)
    }

    /**
     * Enumerates the types of text styles (color, decoration, or reset).
     */
    enum class Type {
        COLOR,
        DECORATION,
        RESET
    }

    companion object {
        private const val COLOR_CHAR = '§'
    }
}
