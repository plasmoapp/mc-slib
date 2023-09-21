package su.plo.slib.permission

import su.plo.slib.api.permission.PermissionTristate

interface PermissionSupplier {

    /**
     * Checks if player has permission
     */
    fun hasPermission(player: Any, permission: String): Boolean

    /**
     * Gets player permission tristate
     */
    fun getPermission(player: Any, permission: String): PermissionTristate
}
