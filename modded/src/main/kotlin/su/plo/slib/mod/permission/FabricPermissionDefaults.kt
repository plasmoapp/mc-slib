@file:Suppress("UnstableApiUsage")

package su.plo.slib.mod.permission

//? if fabric && >=26.4 {
/*import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.permission.v1.PermissionContext
import net.fabricmc.fabric.api.permission.v1.PermissionEvents
import net.fabricmc.fabric.api.permission.v1.PermissionNode
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates
import net.minecraft.commands.CommandSourceStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.players.NameAndId
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.command.brigadier.McRequirements
import su.plo.slib.mod.ModServerLib
import su.plo.slib.util.joinToOneLine
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate

object FabricPermissionDefaults {
    private val defaultsPhase = ResourceLocation.fromNamespaceAndPath("slib", "defaults")

    private val requirementByPermission = ConcurrentHashMap<String, Requirement>()
    private val requirementByKey = ConcurrentHashMap<ResourceLocation, Requirement>()

    private val listener = object : PermissionEvents.OnRequest {
        override fun <T : Any> handlePermissionRequest(context: PermissionContext, permission: PermissionNode<T>): T? {
            val requirement = requirementByKey[permission.key()] ?: return null
            if (requirement.node !== permission) return null

            val granted = defaultValue(context, requirement.permission) ?: return null

            return permission.cast(granted)
        }
    }

    fun register() {
        PermissionEvents.ON_REQUEST.addPhaseOrdering(Event.DEFAULT_PHASE, defaultsPhase)
        PermissionEvents.ON_REQUEST.register(defaultsPhase, listener)
    }

    fun requirement(permission: String): Predicate<CommandSourceStack> =
        requirementByPermission.computeIfAbsent(permission) {
            val key = permission.toPermissionKey()
                ?: throw IllegalArgumentException(
                    """
                        Permission "$permission" can't be used as command requirement since 26.4,
                        it must be "namespace.path" with lowercase [a-z0-9_.-] characters
                    """.joinToOneLine()
                )
            val node = PermissionNode.of(key)

            Requirement(permission, node, PermissionPredicates.require(node))
                .also { requirementByKey[key] = it }
        }.predicate

    private fun defaultValue(context: PermissionContext, permission: String): Boolean? {
        // mirrors ModDefaultCommandSource
        if (context.type() != PermissionContext.Type.PLAYER) return true
        if (!ModServerLib.isBound) return null

        context.sourcePlayer()?.let { return ModServerLib.permissionSupplier.getPermissionDefault(it, permission) }

        val nameAndId = NameAndId(context.uuid(), context.orElse(PermissionContext.NAME, ""))
        val isOp = ModServerLib.minecraftServer.playerList.isOp(nameAndId)

        return ModServerLib.permissionManager.getPermissionDefault(permission).getValue(isOp)
    }

    private class Requirement(
        val permission: String,
        val node: PermissionNode<Boolean>,
        val predicate: Predicate<CommandSourceStack>,
    )
}

private val defaultRequirement: Predicate<*> = LiteralArgumentBuilder.literal<Any>("default").requirement

fun Predicate<McBrigadierSource>.toCommandRequirement(): Predicate<CommandSourceStack>? =
    when {
        this is McRequirements.Permission -> FabricPermissionDefaults.requirement(permission)
        this === defaultRequirement -> null
        else -> throw IllegalArgumentException(
            "raw requires predicates are not supported since Minecraft 26.4, use McRequirements.permission instead"
        )
    }

fun PermissionContext.sourcePlayer(): ServerPlayer? {
    if (type() != PermissionContext.Type.PLAYER) return null

    (get(PermissionContext.ENTITY) as? ServerPlayer)
        ?.takeIf { it.uuid == uuid() }
        ?.let { return it }

    val server = get(PermissionContext.SERVER)
        ?: if (ModServerLib.isBound) ModServerLib.minecraftServer else return null

    return server.playerList.getPlayer(uuid())
}
*///?}
