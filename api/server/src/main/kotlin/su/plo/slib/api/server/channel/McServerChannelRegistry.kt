package su.plo.slib.api.server.channel

import java.util.concurrent.ConcurrentHashMap

/**
 * Registry of custom channel keys for Forge/NeoForge.
 *
 * Forge/NeoForge channels can only be registered on specific loader events,
 * and when you can use [McServerChannelManager.registerChannelHandler] it's already too late.
 * So you should [register] channels in mod/plugin entrypoint before those events are fired.
 * Then slib registers them on Forge/NeoForge, so then they can be used inside of [McServerChannelManager].
 *
 * On other platforms it's no-op.
 */
object McServerChannelRegistry {
    private val channels: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /**
     * Declares a custom [channel] id (e.g. `"pv_groups:channel"`) to be registered early.
     */
    fun register(channel: String) {
        channels.add(channel)
    }

    /**
     * @return A snapshot of the declared channel ids.
     */
    fun channels(): Set<String> = channels.toSet()
}
