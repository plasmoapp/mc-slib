package su.plo.slib.api.command.brigadier

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import su.plo.slib.api.event.command.McBrigadierCommandsRegisterEvent

/**
 * Collects brigadier commands to register in the platform's command dispatcher.
 *
 * This registry is only created at [McBrigadierCommandsRegisterEvent]
 * and discarded after commands are registered in the platform's command dispatcher.
 *
 * The event is fired again for every dispatcher rebuild with [Phase.RUNTIME].
 *
 * Commands have to be buildable without runtime state.
 * Resolve anything that depends on the plugin or mod being initialized inside `executes` bodies.
 */
interface McBrigadierRegistry {
    /**
     * The lifecycle phase this registry is being collected.
     *
     * @see Phase
     */
    val phase: Phase

    /**
     * Registers a brigadier command.
     *
     * @param command     The command to register.
     * @param description The command description shown in help output, or `null` for none.
     *                    Ignored on platforms without a description.
     * @param aliases     Aliases for the command.
     * @throws IllegalArgumentException If a command with the same literal or alias was already registered.
     */
    fun register(command: LiteralCommandNode<McBrigadierSource>, description: String?, aliases: Collection<String>)

    /**
     * Registers a brigadier command.
     *
     * @param command The command to register.
     */
    fun register(command: LiteralCommandNode<McBrigadierSource>) =
        register(command, null, emptyList())

    /**
     * Registers a brigadier command.
     *
     * @param command     The command to register.
     * @param description The command description shown in help output, or `null` for none.
     */
    fun register(command: LiteralCommandNode<McBrigadierSource>, description: String?) =
        register(command, description, emptyList())

    /**
     * Registers a brigadier command.
     *
     * @param command The command to register.
     * @param aliases Aliases for the command.
     */
    fun register(command: LiteralCommandNode<McBrigadierSource>, aliases: Collection<String>) =
        register(command, null, aliases)

    /**
     * Registers a brigadier command.
     *
     * @param command     The command builder to build and register.
     * @param description The command description shown in help output, or `null` for none.
     * @param aliases     Aliases for the command.
     */
    fun register(command: LiteralArgumentBuilder<McBrigadierSource>, description: String?, aliases: Collection<String>) =
        register(command.build(), description, aliases)

    /**
     * Registers a brigadier command.
     *
     * @param command The command builder to build and register.
     */
    fun register(command: LiteralArgumentBuilder<McBrigadierSource>) =
        register(command.build(), null, emptyList())

    /**
     * Registers a brigadier command.
     *
     * @param command     The command builder to build and register.
     * @param description The command description shown in help output, or `null` for none.
     */
    fun register(command: LiteralArgumentBuilder<McBrigadierSource>, description: String?) =
        register(command.build(), description, emptyList())

    /**
     * Registers a brigadier command.
     *
     * @param command The command builder to build and register.
     * @param aliases Aliases for the command.
     */
    fun register(command: LiteralArgumentBuilder<McBrigadierSource>, aliases: Collection<String>) =
        register(command.build(), null, aliases)

    /**
     * At which lifecycle phase a [McBrigadierRegistry] is collected.
     */
    enum class Phase {
        /**
         * The command is being built before the server exists, while datapacks are loading.
         * Registering here is the only way to make a command usable from a plain (non-macro) function line.
         *
         * On modded servers this is the first `CommandRegistrationCallback`/`RegisterCommandsEvent` firing,
         * which happens during the initial resource load.
         * On Paper, it is the bootstrap-owned `LifecycleEvents.COMMANDS` firing.
         *
         * Nothing platform-specific is initialized yet:
         * [McBrigadierSource.isBound] is `false` for sources in this phase,
         * and `requires` predicates are invoked while functions are parsed.
         */
        BOOTSTRAP,

        /**
         * The command is being built with the server available.
         *
         * This is the only phase on proxies and Minestom.
         * On other platforms it follows [BOOTSTRAP], once per dispatcher rebuild of the running server.
         */
        RUNTIME,
    }
}
