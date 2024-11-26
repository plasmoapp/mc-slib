package su.plo.slib.bungee.permission

import net.md_5.bungee.api.connection.ProxiedPlayer
import su.plo.slib.api.permission.PermissionTristate
import su.plo.slib.api.proxy.McProxyLib
import su.plo.slib.permission.PermissionSupplier

class BungeePermissionSupplier(
    private val minecraftProxy: McProxyLib
) : PermissionSupplier {

    override fun hasPermission(player: Any, permission: String): Boolean {
        require(player is ProxiedPlayer) { "player is not " + ProxiedPlayer::class.java }

        val permissionDefault = minecraftProxy.permissionManager.getPermissionDefault(permission)

        return getPermission(player, permission)
            .booleanValue(permissionDefault.getValue(false))
    }

    override fun getPermission(player: Any, permission: String): PermissionTristate {
        require(player is ProxiedPlayer) { "player is not " + ProxiedPlayer::class.java }

        return PermissionTristate.fromBoolean(player.hasPermission(permission))
    }
}
