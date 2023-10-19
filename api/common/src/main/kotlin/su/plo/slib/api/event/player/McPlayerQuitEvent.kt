package su.plo.slib.api.event.player

import su.plo.slib.api.entity.player.McPlayer
import su.plo.slib.api.event.GlobalEvent

/**
 * An event fired when a player quits the server.
 */
object McPlayerQuitEvent
    : GlobalEvent<McPlayerQuitEvent.Callback>(
    { callbacks ->
        Callback { player ->
            callbacks.forEach { callback -> callback.onPlayerQuit(player) }
        }
    }
) {
    fun interface Callback {

        fun onPlayerQuit(player: McPlayer)
    }
}
