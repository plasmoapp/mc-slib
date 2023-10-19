package su.plo.slib.api.event.command

import su.plo.slib.api.McLib
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.event.GlobalEvent

/**
 * Abstract class for registering universal commands.
 *
 * For server use [su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent]
 *
 * For proxy use [su.plo.slib.api.proxy.event.command.McProxyCommandsRegisterEvent]
 *
 * @param C The type of commands to be registered.
 * @param S The type representing the Minecraft server or proxy.
 */
abstract class McCommandsRegisterEvent<C : McCommand, S : McLib>
    : GlobalEvent<McCommandsRegisterEvent.Callback<C, S>>(
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
