package su.plo.slib.api.command.brigadier

import org.jetbrains.annotations.ApiStatus
import su.plo.slib.api.command.brigadier.McRequirements.permission
import java.util.function.Predicate

/**
 * `requires` predicates for commands registered with [McBrigadierRegistry].
 *
 * Modded Minecraft 26.4 has different context for parsing that only accept permission-based requirements,
 * so arbitrary `requires` lambdas can't be represented there and the command fails to register.
 * Use the requirements from this object instead, they work on every platform and version.
 * Dynamic conditions that don't map to a permission belong in `executes`.
 */
object McRequirements {
    /**
     * Creates a requirement that passes if the command source has the [permission].
     *
     * This should be a valid Identifier `namespace:path`, e.g. `pv.allow_freecam`.
     * Only lowercase `a-z`, `0-9`, `_`, `-` and `.` are allowed.
     *
     * Other names make the command fail to register on modded mc 26.4+.
     *
     * Usage: `.requires(McRequirements.permission("pv.allow_freecam"))`
     *
     * @param permission The permission to check.
     * @return A predicate that tests the permission of the command source.
     */
    @JvmStatic
    fun permission(permission: String): Predicate<McBrigadierSource> =
        Permission(permission)

    @ApiStatus.Internal
    class Permission internal constructor(
        val permission: String,
    ) : Predicate<McBrigadierSource> {
        override fun test(source: McBrigadierSource): Boolean =
            source.source.hasPermission(permission)

        override fun toString(): String =
            "McRequirements.permission($permission)"
    }
}
