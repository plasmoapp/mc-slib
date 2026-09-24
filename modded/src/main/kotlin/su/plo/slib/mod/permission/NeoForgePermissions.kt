package su.plo.slib.mod.permission

//? if neoforge {
/*import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.server.permission.PermissionAPI
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent
import net.neoforged.neoforge.server.permission.nodes.PermissionNode
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes
import su.plo.slib.api.permission.PermissionManager
import su.plo.slib.api.permission.PermissionTristate
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

internal object NeoForgePermissions {
    @Volatile
    private var nodeByPermission: Map<String, PermissionNode<Boolean>> = emptyMap()

    private val foreignNodeByPermission = ConcurrentHashMap<String, Optional<PermissionNode<*>>>()

    fun registerNodes(
        event: PermissionGatherEvent.Nodes,
        permissionManager: PermissionManager,
        getPermissionDefault: (ServerPlayer, String) -> Boolean,
    ) {
        val nodes = permissionManager.registeredPermissions
            .mapNotNull { createNode(it, permissionManager, getPermissionDefault) }
            .associateBy { it.nodeName }

        event.addNodes(nodes.values.filterNot { it in event.nodes })
        nodeByPermission = nodes
        foreignNodeByPermission.clear()
    }

    fun getPermission(player: ServerPlayer, permission: String): PermissionTristate {
        val node = nodeByPermission[permission]
            ?: findNodeOfOtherMod(permission)
            ?: return PermissionTristate.UNDEFINED

        return PermissionTristate.fromBoolean(PermissionAPI.getPermission(player, node) as? Boolean)
    }

    private fun findNodeOfOtherMod(permission: String): PermissionNode<*>? =
        foreignNodeByPermission
            .computeIfAbsent(permission) { name ->
                Optional.ofNullable(PermissionAPI.getRegisteredNodes().find { it.nodeName == name })
            }
            .orElse(null)

    private fun createNode(
        permission: String,
        permissionManager: PermissionManager,
        getPermissionDefault: (ServerPlayer, String) -> Boolean,
    ): PermissionNode<Boolean>? {
        if ('*' in permission) return null

        val namespace = permission.substringBefore('.', "")
        val path = permission.substringAfter('.', "")
        if (namespace.isEmpty() || path.isEmpty()) return null

        val defaultResolver = PermissionNode.PermissionResolver { player, _, _ ->
            if (player != null) getPermissionDefault(player, permission)
            else permissionManager.getPermissionDefault(permission).getValue(false)
        }

        return PermissionNode(namespace, path, PermissionTypes.BOOLEAN, defaultResolver)
    }
}
*///?}
