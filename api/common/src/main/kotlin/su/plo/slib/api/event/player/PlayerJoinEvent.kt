package su.plo.slib.api.event.player

import su.plo.slib.api.entity.player.McPlayer
import su.plo.slib.api.event.GlobalEvent

/**
 * This event is fired once the player is joined the server
 */
object PlayerJoinEvent
    : GlobalEvent<PlayerJoinEvent.Callback>(
    { callbacks ->
        Callback { player ->
            callbacks.forEach { callback -> callback.onPlayerJoin(player) }
        }
    }
) {
    fun interface Callback {

        fun onPlayerJoin(player: McPlayer)
    }
}
