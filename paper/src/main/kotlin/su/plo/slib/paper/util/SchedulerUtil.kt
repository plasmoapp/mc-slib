package su.plo.slib.paper.util

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin

object SchedulerUtil {
    /**
     * Schedules a task to run on the main thread.
     */
    fun runTask(plugin: Plugin, task: Runnable) {
        Bukkit.getGlobalRegionScheduler().run(plugin) { task.run() }
    }

    /**
     * Schedules a task to run for a given [entity].
     */
    fun runTaskFor(entity: Entity, plugin: Plugin, task: Runnable) =
        runTaskFor(entity, plugin, task, null)

    /**
     * Schedules a task to run for a given [entity].
     */
    fun runTaskFor(entity: Entity, plugin: Plugin, task: Runnable, retired: Runnable?) {
        val scheduled = entity.scheduler.run(
            plugin,
            { task.run() },
            { retired?.run() },
        )

        if (scheduled == null) retired?.run()
    }

    /**
     * Schedules a task to run for a given [location].
     */
    fun runTaskAt(location: Location, plugin: Plugin, task: Runnable) {
        Bukkit.getRegionScheduler().run(plugin, location) { task.run() }
    }
}
