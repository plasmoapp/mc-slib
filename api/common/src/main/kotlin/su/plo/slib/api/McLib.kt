package su.plo.slib.api

import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.component.McTranslatableText
import su.plo.slib.api.chat.converter.ServerTextConverter
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.language.ServerTranslator
import su.plo.slib.api.logging.McLogger
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.permission.PermissionManager
import java.io.File

/**
 * Represents a Minecraft server or proxy library.
 *
 * This interface serves as the common module for working with Minecraft servers and proxies.
 * Depending on the usage context, specific implementations such as
 * [su.plo.slib.api.server.McServerLib] or [su.plo.slib.api.proxy.McProxyLib]
 * should be used for server or proxy-specific functionality.
 */
interface McLib {

    /**
     * Gets the server translator.
     */
    val serverTranslator: ServerTranslator

    /**
     * Gets the text converter for server-specific text components.
     *
     * The [ServerTextConverter] is responsible for converting [McTextComponent] objects to server-specific text components.
     * It can also translate [McTranslatableText] components using the [ServerTranslator].
     *
     * @return The [ServerTextConverter] for text conversion and translation.
     */
    val textConverter: ServerTextConverter<*>

    /**
     * Gets the command manager for managing and registering commands.
     *
     * Use this to manage server commands.
     *
     * @see McCommandManager
     */
    val commandManager: McCommandManager<*>

    /**
     * Gets the permissions manager for managing universal permissions.
     *
     * The `PermissionsManager` is responsible for registering and managing universal permissions.
     *
     * @see PermissionManager
     */
    val permissionManager: PermissionManager

    /**
     * Gets the folder where plugins/mods configs are stored.
     *
     * @return The folder with plugins/mods configs.
     */
    val configsFolder: File

    /**
     * Creates a new logger with a specified name.
     *
     * @param name The name of the logger.
     */
    fun createLogger(name: String): McLogger =
        McLoggerFactory.createLogger(name).value
}
