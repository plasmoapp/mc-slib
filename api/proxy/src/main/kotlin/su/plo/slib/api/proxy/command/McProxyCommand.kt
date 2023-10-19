package su.plo.slib.api.proxy.command

import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandSource

/**
 * Represents a Minecraft proxy command.
 */
interface McProxyCommand : McCommand {

    /**
     * Checks if the command should be passed to the backend server.
     *
     * Only works with Velocity.
     *
     * @param source The command source that is executing the command.
     * @param args   An array of command arguments, if any.
     * @return `true` if the command should be passed to the backend server, `false` otherwise.
     */
    fun passToBackendServer(source: McCommandSource, args: Array<String>): Boolean {
        return false
    }
}
