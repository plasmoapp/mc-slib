package su.plo.slib.language

import su.plo.slib.api.language.ServerTranslator
import su.plo.slib.integration.IntegrationLoader

object ServerTranslatorFactory {

    fun createTranslator(): ServerTranslator =
        AdventureServerTranslator()
            .also { IntegrationLoader.loadAdventureTranslator(it) }
}
