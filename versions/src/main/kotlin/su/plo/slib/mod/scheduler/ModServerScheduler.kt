package su.plo.slib.mod.scheduler

import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.position.ServerPos3d
import su.plo.slib.api.server.scheduler.McServerScheduler
import su.plo.slib.mod.ModServerLib
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

class ModServerScheduler : McServerScheduler {
    override fun <T> runTask(task: Supplier<T>, loader: Any?): CompletableFuture<T> {
        val future = CompletableFuture<T>()

        ModServerLib.minecraftServer.execute {
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
