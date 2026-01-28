package su.plo.slib.api.server.command.brigadier

import su.plo.slib.api.command.brigadier.ArgumentResolver
import su.plo.slib.api.server.position.ServerPos3d

/**
 * An [ArgumentResolver] that resolves [ServerPos3d].
 */
fun interface ServerPos3dResolver : ArgumentResolver<ServerPos3d>
