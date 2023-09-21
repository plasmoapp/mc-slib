package su.plo.slib.api.command

import su.plo.slib.api.permission.PermissionTristate

/**
 * A holder with set of permissions
 */
interface McPermissionHolder {

    /**
     * Checks if holder has a permission
     *
     * @param permission the permission to check
     * @return true if holder has the permission
     */
    fun hasPermission(permission: String): Boolean

    /**
     * Gets the tristate of a permission
     *
     * @param permission the permission to get
     * @return permission tristate
     */
    fun getPermission(permission: String): PermissionTristate
}
