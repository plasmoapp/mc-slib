package su.plo.slib.integration

import su.plo.slib.api.language.ServerTranslator

object IntegrationLoader {

    fun loadAdventureTranslator(translator: ServerTranslator) {
        try {
            Class.forName("net.kyori.adventure.translation.GlobalTranslator")
            AdventureTranslatorIntegration(translator)
        } catch (_: ClassNotFoundException) {
        }
    }
}