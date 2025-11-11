package su.plo.slib.mod.permission

import net.minecraft.server.level.ServerPlayer
import su.plo.slib.api.permission.PermissionTristate
import su.plo.slib.permission.PermissionSupplier
import su.plo.slib.mod.ModServerLib

//? if fabric {
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.util.TriState
//?}

//? if forge {
/*import net.minecraftforge.server.permission.PermissionAPI
*///?} elif neoforge {
/*import net.neoforged.neoforge.server.permission.PermissionAPI
*///?}

class ModPermissionSupplier(
    private val minecraftServerLib: ModServerLib
) : PermissionSupplier {

    override fun hasPermission(player: Any, permission: String): Boolean {
        require(player is ServerPlayer) { "player is not " + ServerPlayer::class.java }

        val permissionDefault = minecraftServerLib.permissionManager.getPermissionDefault(permission)
        //? if >=1.21.9 {
        /*val isOp = minecraftServerLib.minecraftServer.playerList.isOp(player.nameAndId())
        *///?} else {
        val isOp = minecraftServerLib.minecraftServer.playerList.isOp(player.gameProfile)
        //?}

        return getPermission(player, permission).booleanValue(permissionDefault.getValue(isOp))
    }

    override fun getPermission(player: Any, permission: String): PermissionTristate {
        require(player is ServerPlayer) { "player is not " + ServerPlayer::class.java }

        //? if fabric {
        return toPermissionTristate(Permissions.getPermissionValue(player, permission))
        //?} else {

        /*//? if >=1.18.2 {
        val permissionNode = PermissionAPI.getRegisteredNodes().find { it.nodeName == permission } ?: return PermissionTristate.UNDEFINED
        val value = permissionNode.defaultResolver.resolve(player, player.uuid) as? Boolean

        return PermissionTristate.fromBoolean(value)
        //?} else {
        /^if (!PermissionAPI.getPermissionHandler().registeredNodes.contains(permission))
            return PermissionTristate.UNDEFINED

        return PermissionTristate.fromBoolean(PermissionAPI.hasPermission(player, permission))
        ^///?}
        *///?}
    }

    //? if fabric {
    private fun toPermissionTristate(triState: TriState): PermissionTristate {
        return when (triState) {
            TriState.TRUE -> PermissionTristate.TRUE
            TriState.FALSE -> PermissionTristate.FALSE
            else -> PermissionTristate.UNDEFINED
        }
    }
    //?}
}
