package su.plo.slib.api.event.player

import su.plo.slib.api.entity.player.McPlayer
import su.plo.slib.api.event.GlobalEvent

/**
 * An event fired when visibility of the player checked with [McPlayer.canSee].
 */
object McPlayerVisibilityCheckEvent
    : GlobalEvent<McPlayerVisibilityCheckEvent.Callback>(
    { callbacks ->
        Callback { viewer, target ->
            callbacks.any { callback -> callback.shouldHide(viewer, target) }
        }
    }
) {
    fun interface Callback {
        fun shouldHide(
            viewer: McPlayer,
            target: McPlayer,
        ): Boolean
    }
}
