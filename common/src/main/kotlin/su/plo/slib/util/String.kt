package su.plo.slib.util

fun String.joinToOneLine(): String =
    lines().joinToString(" ") { it.trimIndent() }.trimIndent()
