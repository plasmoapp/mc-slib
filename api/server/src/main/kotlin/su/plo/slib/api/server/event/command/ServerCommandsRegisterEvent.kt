package su.plo.slib.api.server.event.command

import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.event.command.CommandsRegisterEvent

/**
 * This event is fired BEFORE any plugin/mod initialization, so use with caution
 *
 * Register event handler in constructor of your plugin/mod, otherwise it probably will be broken
 */
object ServerCommandsRegisterEvent : CommandsRegisterEvent<McCommand, McServerLib>()
