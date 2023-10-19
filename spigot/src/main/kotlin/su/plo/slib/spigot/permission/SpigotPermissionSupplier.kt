package su.plo.slib.spigot.permission

import org.bukkit.entity.Player
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.permission.PermissionTristate
import su.plo.slib.permission.PermissionSupplier

class SpigotPermissionSupplier(
    private val minecraftServer: McServerLib
) : PermissionSupplier {

    override fun hasPermission(player: Any, permission: String): Boolean {
        require(player is Player) { "player is not ${Player::class.java}" }

        val permissionDefault = minecraftServer.permissionManager.getPermissionDefault(permission)

        return getPermission(player, permission)
            .booleanValue(permissionDefault.getValue(player.isOp))
    }

    override fun getPermission(player: Any, permission: String): PermissionTristate {
        require(player is Player) { "player is not ${Player::class.java}" }

        if (!player.isPermissionSet(permission)) return PermissionTristate.UNDEFINED

        return if (player.hasPermission(permission)) PermissionTristate.TRUE else PermissionTristate.FALSE
    }
}
