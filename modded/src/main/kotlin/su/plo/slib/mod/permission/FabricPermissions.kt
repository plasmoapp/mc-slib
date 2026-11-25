package su.plo.slib.mod.permission

//? if fabric {
import net.minecraft.server.level.ServerPlayer
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.permission.PermissionTristate
import su.plo.slib.mod.ModServerLib

//? if >=26.1
 /*import net.minecraft.resources.ResourceLocation*/

interface FabricPermissions {
    fun getPermission(player: ServerPlayer, permission: String): PermissionTristate
}

object FabricPermissionsProvider {
    private val logger = McLoggerFactory.createLogger(ModServerLib.baseLogger, "FabricPermissionsProvider")

    private val backends: List<FabricPermissions> by lazy {
        val backends = buildList {
            //? if >=26.1 {
            /*if (isClassPresent("net.fabricmc.fabric.api.permission.v1.PermissionEvents")) add(FabricPermissionApi)
            *///?}
            //? if <26.4 {
            if (isClassPresent("me.lucko.fabric.api.permissions.v0.Permissions")) add(LuckoPermissionsApi)
            //?}
        }

        if (backends.isEmpty()) {
            logger.warn("No permission API found, it's not intended to happen, so if you see this message, report it to https://github.com/plasmoapp/mc-slib with logs attached")
        }

        backends
    }

    fun getPermission(player: ServerPlayer, permission: String): PermissionTristate {
        backends.forEach { backend ->
            val tristate = backend.getPermission(player, permission)
            if (tristate != PermissionTristate.UNDEFINED) return tristate
        }

        return PermissionTristate.UNDEFINED
    }

    private fun isClassPresent(className: String) =
        try {
            Class.forName(className)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
}

//? if >=26.1 {
/*fun String.toPermissionKey(): ResourceLocation? {
    val namespace = substringBefore('.', "")
    val path = substringAfter('.', "")
    if (namespace.isEmpty() || path.isEmpty()) return null

    return ResourceLocation.tryBuild(namespace, path)
}
*///?}
//?}
