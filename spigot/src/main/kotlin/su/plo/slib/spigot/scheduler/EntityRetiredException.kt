package su.plo.slib.spigot.scheduler

import java.util.UUID

/**
 * Thrown when a task scheduled for an entity can't run because the entity was removed.
 */
class EntityRetiredException(entityId: UUID) :
    IllegalStateException("Entity $entityId was removed before the scheduled task could run")
