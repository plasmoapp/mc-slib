package su.plo.slib.spigot.scheduler

import org.bukkit.plugin.Plugin
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.position.ServerPos3d
import su.plo.slib.api.server.scheduler.McServerScheduler
import su.plo.slib.spigot.extension.toLocation
import su.plo.slib.spigot.util.SchedulerUtil
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

class SpigotServerScheduler(
    private val loader: Plugin,
) : McServerScheduler {
    override fun <T> runTask(task: Supplier<T>, loader: Any?): CompletableFuture<T> {
        val plugin = loader?.let { it as? Plugin } ?: this.loader

        val future = CompletableFuture<T>()

        SchedulerUtil.runTask(plugin) {
            runCatching {
                task.get()
            }
                .onSuccess { future.complete(it) }
                .onFailure { future.completeExceptionally(it) }
        }

        return future
    }

    override fun <T> runTaskAt(location: ServerPos3d, task: Supplier<T>, loader: Any?): CompletableFuture<T> {
        val plugin = loader?.let { it as? Plugin } ?: this.loader

        val future = CompletableFuture<T>()

        SchedulerUtil.runTaskAt(location.toLocation(), plugin) {
            runCatching {
                task.get()
            }
                .onSuccess { future.complete(it) }
                .onFailure { future.completeExceptionally(it) }
        }

        return future
    }

    override fun <T> runTaskFor(entity: McServerEntity, task: Supplier<T>, loader: Any?): CompletableFuture<T> {
        val plugin = loader?.let { it as? Plugin } ?: this.loader

        val future = CompletableFuture<T>()

        SchedulerUtil.runTaskFor(entity.getInstance(), plugin) {
            runCatching {
                task.get()
            }
                .onSuccess { future.complete(it) }
                .onFailure { future.completeExceptionally(it) }
        }

        return future
    }
}
