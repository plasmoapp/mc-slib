package su.plo.slib.language

import su.plo.slib.api.language.ServerTranslator

object ServerTranslatorFactory {

    fun createTranslator(): ServerTranslator =
        try {
            Class.forName("net.kyori.adventure.translation.GlobalTranslator")
            AdventureServerTranslator()
        } catch (_: ClassNotFoundException) {
            MapServerTranslator()
        }
}
