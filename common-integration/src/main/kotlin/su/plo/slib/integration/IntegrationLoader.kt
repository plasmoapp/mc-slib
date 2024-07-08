package su.plo.slib.integration

import su.plo.slib.api.language.ServerTranslator
import su.plo.slib.language.AdventureServerTranslator

object IntegrationLoader {

    fun loadAdventureTranslator(translator: ServerTranslator) {
        try {
            Class.forName("net.kyori.adventure.translation.GlobalTranslator")
            AdventureTranslatorIntegration(translator as AdventureServerTranslator)
        } catch (_: ClassNotFoundException) {
        }
    }
}