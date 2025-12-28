package su.plo.slib.api.command.brigadier

/**
 * Defers argument resolution until command execution.
 *
 * Used with [CustomArgumentType] to parse arguments into selectors at parse time,
 * then resolve them to actual objects at execution time using the command source.
 *
 * @param T the resolved type
 */
fun interface ArgumentResolver<T> {
    /**
     * Resolves the argument using the command [source].
     *
     * @param source the command source
     * @return the resolved value
     */
    fun resolve(source: McBrigadierSource): T
}
