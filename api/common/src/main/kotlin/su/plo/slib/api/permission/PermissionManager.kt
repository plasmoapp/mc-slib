package su.plo.slib.api.permission

import com.google.common.collect.Maps
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.style.McTextStyle

/**
 * Manages universal permissions.
 *
 * This class managing universal permissions that are independent of server implementations.
 */
class PermissionManager {

    private val defaultPermissionByName: MutableMap<String, PermissionDefault?> = Maps.newHashMap()

    /**
     * Gets or sets "no permission" message.
     */
    var noPermissionMessage = McTextComponent
        .literal("I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.")
        .withStyle(McTextStyle.RED)

    /**
     * Registers a universal permission with the specified name and default value.
     *
     * @param name              The name of the permission to register.
     * @param permissionDefault The default value of the permission.
     */
    fun register(name: String, permissionDefault: PermissionDefault) {
        defaultPermissionByName[name] = permissionDefault
    }

    /**
     * Unregisters a universal permission by name.
     *
     * @param name The name of the permission to unregister.
     * @return `true` if the permission was successfully removed, `false` otherwise.
     */
    fun unregister(name: String): Boolean {
        return defaultPermissionByName.remove(name) != null
    }

    /**
     * Clears all universal permissions.
     */
    fun clear() {
        defaultPermissionByName.clear()
    }

    /**
     * Gets the default permission value by permission name.
     *
     * @param name The name of the permission to retrieve.
     * @return The default value of the permission, or [PermissionDefault.OP] if the permission does not exist.
     */
    fun getPermissionDefault(name: String): PermissionDefault {
        return defaultPermissionByName.getOrDefault(name, DEFAULT_PERMISSION)!!
    }

    companion object {
        @JvmStatic
        private val DEFAULT_PERMISSION = PermissionDefault.OP
    }
}
