package su.plo.slib.minestom.permission

import net.minestom.server.entity.Player
import net.minestom.server.permission.Permission
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.permission.PermissionTristate
import su.plo.slib.permission.PermissionSupplier

class MinestomPermissionSupplier(
    private val minecraftServer: McServerLib
) : PermissionSupplier {

    override fun hasPermission(player: Any, permission: String): Boolean {
        require(player is Player) { "player is not ${Player::class.java}" }

        val permissionDefault = minecraftServer.permissionManager.getPermissionDefault(permission)

        return getPermission(player, permission)
            .booleanValue(permissionDefault.getValue(false)) // Minestom does not have an OP feature
    }

    override fun getPermission(player: Any, permission: String): PermissionTristate {
        require(player is Player) { "player is not ${Player::class.java}" }

        if (!player.allPermissions.contains(Permission(permission))) return PermissionTristate.UNDEFINED

        return if (player.hasPermission(permission)) PermissionTristate.TRUE else PermissionTristate.FALSE
    }
}
