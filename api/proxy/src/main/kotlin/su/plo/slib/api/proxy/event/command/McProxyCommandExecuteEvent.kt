package su.plo.slib.api.proxy.event.command

import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.event.GlobalEvent

/**
 * An event fired before command execution.
 */
object McProxyCommandExecuteEvent
    : GlobalEvent<McProxyCommandExecuteEvent.Callback>(
    { callbacks ->
        Callback { source, command ->
            callbacks.forEach { callback -> callback.onCommandExecute(source, command) }
        }
    }
) {
    fun interface Callback {

        fun onCommandExecute(source: McCommandSource, command: String)
    }
}
