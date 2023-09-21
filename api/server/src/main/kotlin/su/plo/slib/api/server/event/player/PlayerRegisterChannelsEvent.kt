package su.plo.slib.api.server.event.player

import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.api.event.GlobalEvent
import su.plo.slib.api.server.event.player.PlayerRegisterChannelsEvent.Callback

/**
 * This event is fired once the player register custom channels
 */
object PlayerRegisterChannelsEvent
    : GlobalEvent<Callback>(
    { callbacks ->
        Callback { player, channels ->
            callbacks.forEach { callback -> callback.onPlayerRegisterChannels(player, channels) }
        }
    }
) {
    fun interface Callback {

        fun onPlayerRegisterChannels(player: McServerPlayer, channels: List<String>)
    }
}
