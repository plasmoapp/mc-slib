package su.plo.slib.mod.permission

//? if fabric {
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.util.TriState
import net.minecraft.server.level.ServerPlayer
import su.plo.slib.api.permission.PermissionTristate

internal object LuckoPermissionsApi : FabricPermissions {
    override fun getPermission(player: ServerPlayer, permission: String) =
        when (Permissions.getPermissionValue(player, permission)) {
            TriState.TRUE -> PermissionTristate.TRUE
            TriState.FALSE -> PermissionTristate.FALSE
            else -> PermissionTristate.UNDEFINED
        }
}
//?}
