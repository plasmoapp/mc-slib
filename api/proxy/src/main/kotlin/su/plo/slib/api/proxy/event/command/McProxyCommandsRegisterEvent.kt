package su.plo.slib.api.proxy.event.command

import su.plo.slib.api.event.command.McCommandsRegisterEvent
import su.plo.slib.api.proxy.McProxyLib
import su.plo.slib.api.proxy.command.McProxyCommand

/**
 * An event fired before any plugin or mod initialization for command registration.
 * Ensure that you register event handler in the constructor of your plugin or mod
 * to properly handle this event.
 */
object McProxyCommandsRegisterEvent : McCommandsRegisterEvent<McProxyCommand, McProxyLib>()
