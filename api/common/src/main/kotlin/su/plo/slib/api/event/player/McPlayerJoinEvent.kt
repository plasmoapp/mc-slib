package su.plo.slib.api.event.player

import su.plo.slib.api.entity.player.McPlayer
import su.plo.slib.api.event.GlobalEvent

/**
 * An event fired when a player joins the server.
 */
object McPlayerJoinEvent
    : GlobalEvent<McPlayerJoinEvent.Callback>(
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
