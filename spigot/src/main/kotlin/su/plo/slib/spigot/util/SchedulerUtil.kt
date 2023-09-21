package su.plo.slib.spigot.util

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

object SchedulerUtil {

    /**
     * Schedules a task to run for a given [entity].
     *
     * For non-Folia servers, runs on Bukkit scheduler.
     * For Folia servers, runs on the entity's scheduler.
     */
    @Suppress("deprecation")
    fun runTaskFor(entity: Entity, plugin: Plugin, task: Runnable) {
        try {
            entity.scheduler.run(plugin, { task.run() }, null)
        } catch (e: ReflectiveOperationException) {
            Bukkit.getScheduler().runTask(plugin, task)
        }
    }

    /**
     * Schedules a task to run for a given [location].
     *
     * For non-Folia servers, runs on Bukkit scheduler.
     * For Folia servers, runs on the region's scheduler.
     */
    @Suppress("deprecation")
    fun runTaskAt(location: Location, plugin: Plugin, task: Runnable) {
        try {
            Bukkit.getRegionScheduler().run(plugin, location) { task.run() }
        } catch (e: ReflectiveOperationException) {
            Bukkit.getScheduler().runTask(plugin, task)
        }
    }
}
