package su.plo.slib.api.event.player

import su.plo.slib.api.entity.player.McPlayer
import su.plo.slib.api.event.GlobalEvent

/**
 * An event fired when visibility of the player changed.
 */
object McPlayerVisibilityChangedEvent
    : GlobalEvent<McPlayerVisibilityChangedEvent.Callback>(
    { callbacks ->
        Callback { target, hidden ->
            callbacks.forEach { callback -> callback.onVisibilityChanged(target, hidden) }
        }
    }
) {
    fun interface Callback {
        fun onVisibilityChanged(
            target: McPlayer,
            hidden: Boolean,
        )
    }
}
