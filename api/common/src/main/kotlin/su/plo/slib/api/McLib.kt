package su.plo.slib.api

import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.component.McTranslatableText
import su.plo.slib.api.chat.converter.ServerTextConverter
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.language.ServerLanguages
import su.plo.slib.api.permission.PermissionsManager

/**
 * Represents a Minecraft server
 *
 * Use it when you are using ONLY common module
 *
 * For proxy use MinecraftServerLib
 * For server use MinecraftProxyLib
 */
interface McLib {

    /**
     * Gets server languages
     *
     * @see ServerLanguages
     */
    val languages: ServerLanguages

    /**
     * Gets the text converter
     *
     * Text converter used to convert [McTextComponent] to server's specific text component
     *
     * [ServerTextConverter] can translate [McTranslatableText] by using
     * [ServerLanguages] ([ServerTextConverter.convert])
     *
     * @return [ServerTextConverter]
     */
    val textConverter: ServerTextConverter<*>

    /**
     * @see McCommandManager
     */
    val commandManager: McCommandManager<*>

    /**
     * @see PermissionsManager
     */
    val permissionsManager: PermissionsManager
}
