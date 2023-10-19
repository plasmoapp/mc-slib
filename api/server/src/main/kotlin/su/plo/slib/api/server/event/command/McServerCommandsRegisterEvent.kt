package su.plo.slib.api.server.event.command

import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.event.command.McCommandsRegisterEvent

/**
 * An event fired before any plugin or mod initialization for command registration.
 * Ensure that you register event handler in the constructor of your plugin or mod
 * to properly handle this event.
 */
object McServerCommandsRegisterEvent : McCommandsRegisterEvent<McCommand, McServerLib>()
