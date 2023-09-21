package su.plo.slib.api.proxy.command

import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandSource

interface McProxyCommand : McCommand {

    /**
     * Pass command to backend server
     *
     * Only works with Velocity
     */
    fun passToBackendServer(source: McCommandSource, args: Array<String?>?): Boolean {
        return false
    }
}
