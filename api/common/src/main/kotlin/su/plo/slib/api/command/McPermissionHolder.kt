package su.plo.slib.api.command

import su.plo.slib.api.permission.PermissionTristate

/**
 * Represents a holder with a set of permissions.
 */
interface McPermissionHolder {

    /**
     * Checks if the holder has a specific permission.
     *
     * @param permission The permission to check.
     * @return `true` if the holder has the specified permission; otherwise, `false`.
     */
    fun hasPermission(permission: String): Boolean

    /**
     * Gets the tristate of a specific permission.
     *
     * @param permission The permission to retrieve the tristate for.
     * @return The tristate of the specified permission.
     */
    fun getPermission(permission: String): PermissionTristate
}
