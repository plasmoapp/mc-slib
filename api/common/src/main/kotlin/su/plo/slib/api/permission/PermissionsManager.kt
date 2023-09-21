package su.plo.slib.api.permission

import com.google.common.collect.Maps

/**
 * Manages universal permissions
 *
 * Universal permissions are server implementation independent, so they will work on Paper/Forge/Fabric/etc.
 */
class PermissionsManager {

    private val defaultPermissionByName: MutableMap<String, PermissionDefault?> = Maps.newHashMap()

    /**
     * Registers the universal permission
     *
     * @param name permission name
     * @param permissionDefault permission default value
     */
    fun register(name: String, permissionDefault: PermissionDefault) {
        defaultPermissionByName[name] = permissionDefault
    }

    /**
     * @return true if the map contained the given permission [name]
     */
    fun unregister(name: String): Boolean {
        return defaultPermissionByName.remove(name) != null
    }

    /**
     * Clears all universal permissions
     */
    fun clear() {
        defaultPermissionByName.clear()
    }

    /**
     * Gets the default permission value by permission name
     *
     * @return value if exists or [PermissionDefault.OP]
     */
    fun getPermissionDefault(name: String): PermissionDefault {
        return defaultPermissionByName.getOrDefault(name, DEFAULT_PERMISSION)!!
    }

    companion object {
        private val DEFAULT_PERMISSION = PermissionDefault.OP
    }
}
