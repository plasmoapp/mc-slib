package su.plo.slib.spigot.util

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

object SchedulerUtil {
    @Volatile private var regionSchedulerSupported: Boolean? = null

    /**
     * Schedules a task to run on the main thread.
     *
     * For non-Folia servers, runs on Bukkit scheduler.
     * For Folia servers, runs on the global region's scheduler.
     */
    fun runTask(plugin: Plugin, task: Runnable) {
        if (regionSchedulerSupported == false) {
            Bukkit.getScheduler().runTask(plugin, task)
            return
        }

        try {
            Bukkit.getGlobalRegionScheduler().run(plugin) { task.run() }
            regionSchedulerSupported = true
        } catch (_: LinkageError) {
            Bukkit.getScheduler().runTask(plugin, task)
            regionSchedulerSupported = false
        }
    }

    /**
     * Schedules a task to run for a given [entity].
     *
     * For non-Folia servers, runs on Bukkit scheduler.
     * For Folia servers, runs on the entity's scheduler.
     */
    fun runTaskFor(entity: Entity, plugin: Plugin, task: Runnable) {
        if (regionSchedulerSupported == false) {
            Bukkit.getScheduler().runTask(plugin, task)
            return
        }

        try {
            entity.scheduler.run(
                plugin,
                { task.run() },
                null,
            )
            regionSchedulerSupported = true
        } catch (_: LinkageError) {
            Bukkit.getScheduler().runTask(plugin, task)
            regionSchedulerSupported = false
        }
    }

    /**
     * Schedules a task to run for a given [location].
     *
     * For non-Folia servers, runs on Bukkit scheduler.
     * For Folia servers, runs on the region's scheduler.
     */
    fun runTaskAt(location: Location, plugin: Plugin, task: Runnable) {
        if (regionSchedulerSupported == false) {
            Bukkit.getScheduler().runTask(plugin, task)
            return
        }

        try {
            Bukkit.getRegionScheduler().run(plugin, location) { task.run() }
            regionSchedulerSupported = true
        } catch (_: LinkageError) {
            Bukkit.getScheduler().runTask(plugin, task)
            regionSchedulerSupported = false
        }
    }
}
