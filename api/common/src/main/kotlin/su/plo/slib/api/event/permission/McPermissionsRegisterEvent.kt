package su.plo.slib.api.event.permission

import su.plo.slib.api.event.GlobalEvent
import su.plo.slib.api.permission.PermissionDefault
import su.plo.slib.api.permission.PermissionManager

/**
 * An event fired on every server or proxy start for permission registration.
 *
 * Register every permission you check here.
 * Some platforms (NeoForge's `PermissionAPI`) only accept permissions declared at this point.
 * Permission registered later still resolves to its [PermissionDefault],
 * but permission handlers such as LuckPerms never see it.
 *
 * Register the listener as early as possible, from the constructor of your plugin or mod.
 */
object McPermissionsRegisterEvent
    : GlobalEvent<McPermissionsRegisterEvent.Callback>(
    { callbacks ->
        Callback { permissionManager ->
            callbacks.forEach { callback -> callback.onPermissionsRegister(permissionManager) }
        }
    }
) {
    fun interface Callback {
        fun onPermissionsRegister(permissionManager: PermissionManager)
    }
}
