package su.plo.slib.api.server.command.brigadier

import su.plo.slib.api.command.brigadier.ArgumentResolver
import su.plo.slib.api.server.entity.McServerEntity

/**
 * An [ArgumentResolver] that resolves multiple entities.
 */
fun interface McEntitiesArgumentResolver : ArgumentResolver<List<McServerEntity>>
