package su.plo.slib.velocity.permission

import com.velocitypowered.api.proxy.Player
import su.plo.slib.api.permission.PermissionTristate
import su.plo.slib.api.proxy.McProxyLib
import su.plo.slib.permission.PermissionSupplier
import su.plo.slib.velocity.extension.toPermissionTristate

class VelocityPermissionSupplier(
    private val minecraftProxy: McProxyLib
) : PermissionSupplier {

    override fun hasPermission(player: Any, permission: String): Boolean {
        require(player is Player) { "player is not " + Player::class.java }

        val permissionDefault = minecraftProxy.permissionManager.getPermissionDefault(permission)

        return getPermission(player, permission)
            .booleanValue(permissionDefault.getValue(false))
    }

    override fun getPermission(player: Any, permission: String): PermissionTristate {
        require(player is Player) { "player is not " + Player::class.java }

        return player.getPermissionValue(permission).toPermissionTristate()
    }
}
