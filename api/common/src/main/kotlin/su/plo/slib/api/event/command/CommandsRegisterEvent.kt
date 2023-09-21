package su.plo.slib.api.event.command

import su.plo.slib.api.McLib
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.event.GlobalEvent

/**
 * Abstract class for universal commands registration
 *
 * For server use ServerCommandsRegisterEvent
 *
 * For proxy use ProxyCommandsRegisterEvent
 */
abstract class CommandsRegisterEvent<C : McCommand, S : McLib>
    : GlobalEvent<CommandsRegisterEvent.Callback<C, S>>(
    { callbacks ->
        Callback { commandManager, minecraftServer ->
            callbacks.forEach { callback -> callback.onCommandsRegister(commandManager, minecraftServer) }
        }
    }
) {

    fun interface Callback<C : McCommand, S : McLib> {

        fun onCommandsRegister(commandManager: McCommandManager<C>, minecraftServer: S)
    }
}
