package su.plo.slib.minestom.scheduler

import net.minestom.server.MinecraftServer
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.position.ServerPos3d
import su.plo.slib.api.server.scheduler.McServerScheduler
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

class MinestomServerScheduler : McServerScheduler {
    override fun <T> runTask(task: Supplier<T>, loader: Any?): CompletableFuture<T> {
        val future = CompletableFuture<T>()

        MinecraftServer.getSchedulerManager().scheduleNextTick {
            runCatching {
                task.get()
            }
                .onSuccess { future.complete(it) }
                .onFailure { future.completeExceptionally(it) }
        }

        return future
    }

    override fun <T> runTaskFor(entity: McServerEntity, task: Supplier<T>, loader: Any?): CompletableFuture<T> =
        runTask(task, loader)

    override fun <T> runTaskAt(location: ServerPos3d, task: Supplier<T>, loader: Any?): CompletableFuture<T> =
        runTask(task, loader)
}
