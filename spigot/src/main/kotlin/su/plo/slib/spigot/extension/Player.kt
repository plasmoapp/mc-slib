package su.plo.slib.spigot.extension

import org.bukkit.entity.Player

/**
 * Adds the channel to [Player.getListeningPluginChannels] using reflections.
 */
fun Player.addChannel(channel: String) {
    val addChannelMethod = try {
        this.javaClass.getDeclaredMethod("addChannel", String::class.java)
    } catch (e: ReflectiveOperationException) {
        return // huh?
    }

    addChannelMethod.invoke(this, channel)
}
