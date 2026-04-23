package su.plo.slib.minestom.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.Translator
import net.minestom.server.command.CommandSender
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.converter.ServerTextConverter
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.permission.PermissionTristate
import java.util.Locale

class MinestomDefaultCommandSource(
    private val textConverter: ServerTextConverter<*>,
    private val source: CommandSender
) : McCommandSource {

    override fun sendMessage(text: McTextComponent) {
        source.sendMessage(render(text))
    }

    override fun sendActionBar(text: McTextComponent) {
        source.sendActionBar(render(text))
    }

    private fun render(text: McTextComponent): Component {
        val json = textConverter.convertToJson(this, text)
        val component = GsonComponentSerializer.gson().deserialize(json)
        val locale = Translator.parseLocale(language) ?: Locale.US
        return GlobalTranslator.render(component, locale)
    }

    override val language: String
        get() = "en_us"

    override fun hasPermission(permission: String): Boolean = true

    override fun getPermission(permission: String): PermissionTristate = PermissionTristate.FALSE
}
