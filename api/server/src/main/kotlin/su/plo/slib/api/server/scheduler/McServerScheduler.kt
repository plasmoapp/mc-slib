package su.plo.slib.api.server.scheduler

import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.position.ServerPos3d
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

interface McServerScheduler {

    /**
     * Schedules a task to run on the game tick thread.
     *
     * @param task Task that will be scheduled for the execution.
     * @param loader Loader object identified with this task.
     */
    fun <T> runTask(task: Supplier<T>, loader: Any? = null): CompletableFuture<T>

    /**
     * Schedules a task to run on the game tick thread.
     *
     * @param task Task that will be scheduled for the execution.
     */
    fun <T> runTask(task: Supplier<T>): CompletableFuture<T> =
        runTask(task, null)

    /**
     * Schedules a task to run on the game tick thread for a given [entity].
     *
     * @param entity Entity to execute the task with.
     * @param task Task that will be scheduled for the execution.
     * @param loader Loader object identified with this task.
     */
    fun <T> runTaskFor(entity: McServerEntity, task: Supplier<T>, loader: Any? = null): CompletableFuture<T>

    /**
     * Schedules a task to run on the game tick thread for a given [entity].
     *
     * @param entity Entity to execute the task with.
     * @param task Task that will be scheduled for the execution.
     */
    fun <T> runTaskFor(entity: McServerEntity, task: Supplier<T>): CompletableFuture<T> =
        runTaskFor(entity, task, null)

    /**
     * Schedules a task to run on the game tick thread at a given [location].
     *
     * @param location Location to execute the task at.
     * @param task Task that will be scheduled for the execution.
     * @param loader Loader object identified with this task.
     */
    fun <T> runTaskAt(location: ServerPos3d, task: Supplier<T>, loader: Any? = null): CompletableFuture<T>

    /**
     * Schedules a task to run on the game tick thread at a given [location].
     *
     * @param location Location to execute the task at.
     * @param task Task that will be scheduled for the execution.
     */
    fun <T> runTaskAt(location: ServerPos3d, task: Supplier<T>): CompletableFuture<T> =
        runTaskAt(location, task, null)
}
