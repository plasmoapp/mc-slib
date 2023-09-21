package su.plo.slib.api.event.player

import su.plo.slib.api.entity.player.McPlayer
import su.plo.slib.api.event.GlobalEvent

/**
 * This event is fires once the player is disconnected from the server
 */
object PlayerQuitEvent
    : GlobalEvent<PlayerQuitEvent.Callback>(
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
