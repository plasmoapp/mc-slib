@file:Suppress("UnstableApiUsage")

package su.plo.slib.paper.command

import io.papermc.paper.plugin.configuration.PluginMeta
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

// paper doesn't unregister commands in dispatcher when plugin is disabled
// (eg crashed on `onEnable` due to malformed config or something else),
// causing full server crash due to classloader errors when trying to run `requires` on command of disabled plugin
//
// to prevent that, we're checking if plugin is actually enabled here
internal class PaperPluginBinding(private val owner: PluginMeta) {
    @Volatile
    private var plugin: Plugin? = null

    fun isUnbound(): Boolean {
        // Bukkit#getServer is null in bootstrap phase
        @Suppress("USELESS_ELVIS")
        val server = Bukkit.getServer() ?: return false

        val owningPlugin = plugin
            ?: server.pluginManager.getPlugin(owner.name)?.also { plugin = it }
            ?: return false

        return !owningPlugin.isEnabled
    }
}
