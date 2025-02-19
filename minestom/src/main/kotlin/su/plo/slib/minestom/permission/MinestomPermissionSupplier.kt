package su.plo.slib.minestom.permission

import net.minestom.server.entity.Player
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

    // todo: permissions support?
    //  minestom removed built-in permissions, so something like LuckPerms should be used directly
    override fun getPermission(player: Any, permission: String): PermissionTristate =
        PermissionTristate.UNDEFINED
}
