package su.plo.slib.api.server.command.brigadier

import su.plo.slib.api.command.brigadier.ArgumentResolver
import su.plo.slib.api.server.entity.player.McServerPlayer

/**
 * An [ArgumentResolver] that resolves a single player.
 */
fun interface McPlayerArgumentResolver : ArgumentResolver<McServerPlayer>
