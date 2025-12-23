package su.plo.slib.api.command.brigadier

import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.entity.McEntity

interface McBrigadierContext {
    /**
     * Gets the command source that initiated/triggered the execution of a command.
     */
    val source: McCommandSource

    /**
     * Gets the entity executing this command.
     */
    val executor: McEntity?
}
