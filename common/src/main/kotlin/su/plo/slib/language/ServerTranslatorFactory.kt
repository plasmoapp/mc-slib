package su.plo.slib.language

import su.plo.slib.api.language.ServerTranslator

object ServerTranslatorFactory {

    fun createTranslator(): ServerTranslator =
        AdventureServerTranslator()
}
