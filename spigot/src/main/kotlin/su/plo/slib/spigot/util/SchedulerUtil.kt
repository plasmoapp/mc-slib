package su.plo.slib.spigot.util

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import java.util.function.Consumer

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
            val entityScheduler = entity.javaClass.getMethod("getScheduler").invoke(entity)
            val runMethod = entityScheduler.javaClass.getMethod(
                "run",
                Plugin::class.java, Consumer::class.java, Runnable::class.java
            )

            runMethod.invoke(entityScheduler, plugin, Consumer<Any> { task.run() }, null)

            // entity.scheduler.run(plugin, { task.run() }, null)
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
            val regionScheduler = Bukkit::class.java.getMethod("getRegionScheduler").invoke(null)
            val runMethod = regionScheduler.javaClass.getMethod(
                "run",
                Plugin::class.java, Location::class.java, Consumer::class.java
            )

            runMethod.invoke(regionScheduler, plugin, location, Consumer<Any> { task.run() })

            // Bukkit.getRegionScheduler().run(plugin, location) { task.run() }
        } catch (e: ReflectiveOperationException) {
            Bukkit.getScheduler().runTask(plugin, task)
        }
    }
}
