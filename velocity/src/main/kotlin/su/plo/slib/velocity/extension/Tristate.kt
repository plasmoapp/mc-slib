package su.plo.slib.velocity.extension

import com.velocitypowered.api.permission.Tristate
import su.plo.slib.api.permission.PermissionTristate

fun Tristate.toPermissionTristate(): PermissionTristate =
    when (this) {
        Tristate.TRUE -> PermissionTristate.TRUE
        Tristate.FALSE -> PermissionTristate.FALSE
        Tristate.UNDEFINED -> PermissionTristate.UNDEFINED
    }
