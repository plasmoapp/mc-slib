package su.plo.slib.mod.permission

//? if fabric && >=26.1 {
/*import net.fabricmc.fabric.api.permission.v1.PermissionContextOwner
import net.fabricmc.fabric.api.permission.v1.PermissionNode
import net.minecraft.server.level.ServerPlayer
import su.plo.slib.api.permission.PermissionTristate
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

internal object FabricPermissionApi : FabricPermissions {
    private val nodeByPermission = ConcurrentHashMap<String, Optional<PermissionNode<Boolean>>>()

    override fun getPermission(player: ServerPlayer, permission: String): PermissionTristate {
        val node = nodeByPermission
            .computeIfAbsent(permission) { Optional.ofNullable(createNode(it)) }
            .orElse(null)
            ?: return PermissionTristate.UNDEFINED

        val permissionOwner = (player as? PermissionContextOwner) ?: return PermissionTristate.UNDEFINED

        return PermissionTristate.fromBoolean(permissionOwner.checkPermission(node))
    }

    private fun createNode(permission: String): PermissionNode<Boolean>? {
        val namespace = permission.substringBefore('.', "")
        val path = permission.substringAfter('.', "")
        if (namespace.isEmpty() || path.isEmpty()) return null

        return PermissionNode.of(namespace, path)
    }
}
*///?}
