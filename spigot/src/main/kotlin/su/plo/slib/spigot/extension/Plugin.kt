package su.plo.slib.spigot.extension

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import su.plo.slib.spigot.util.SchedulerUtil

fun Plugin.runSync(entity: Entity, runnable: Runnable) =
    SchedulerUtil.runTaskFor(entity, this, runnable)

fun Plugin.runSync(location: Location, runnable: Runnable) =
    SchedulerUtil.runTaskAt(location, this, runnable)
