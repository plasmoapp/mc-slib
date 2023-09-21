package su.plo.slib.api.chat.style

enum class McTextStyle(
    val type: Type,
    val colorChar: Char
) {

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
    OBFUSCATED(Type.DECORATION, 'k'),
    BOLD(Type.DECORATION, 'l'),
    STRIKETHROUGH(Type.DECORATION, 'm'),
    UNDERLINE(Type.DECORATION, 'n'),
    ITALIC(Type.DECORATION, 'o'),
    RESET(Type.RESET, 'r');

    override fun toString(): String {
        return String.format("%s%s", COLOR_CHAR, colorChar)
    }

    enum class Type {
        COLOR,
        DECORATION,
        RESET // ??
    }

    companion object {
        private const val COLOR_CHAR = '§'
    }
}
