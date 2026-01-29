package su.plo.slib.api.server.command.brigadier

import su.plo.slib.api.command.brigadier.ArgumentResolver
import su.plo.slib.api.entity.player.McGameProfile

/**
 * An [ArgumentResolver] that resolves multiple game profiles.
 */
fun interface McGameProfilesArgumentResolver : ArgumentResolver<List<McGameProfile>>
