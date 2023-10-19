package su.plo.slib.api.command

import com.google.common.collect.ImmutableList

/**
 * Represents a Minecraft command to be executed by a [McCommandSource].
 *
 * Command should implement the [execute] method to specify the behavior of the command when executed,
 * and optionally provide suggestions and permission checks.
 */
interface McCommand {

    /**
     * Invoked when a [McCommandSource] executes the command.
     *
     * @param source     The command source that is executing the command.
     * @param arguments  An array of command arguments provided when executing the command.
     */
    fun execute(source: McCommandSource, arguments: Array<String>)

    /**
     * Provides a list of command suggestions based on the current input.
     *
     * By default, it returns an empty list.
     *
     * @param source     The command source requesting suggestions.
     * @param arguments  An array of command arguments provided by the user for generating suggestions.
     * @return A list of suggested command inputs.
     */
    fun suggest(source: McCommandSource, arguments: Array<String>): List<String> {
        return ImmutableList.of()
    }

    /**
     * Checks if the command source has permission to execute this command.
     *
     * Note: Arguments are not null only during command execution.
     *
     * By default, this method returns `true`, indicating that the command can be executed by any source.
     *
     * @param source     The command source attempting to execute the command.
     * @param arguments  An array of command arguments provided during execution (may be null).
     * @return `true` if the command source has permission to execute this command; otherwise, `false`.
     */
    fun hasPermission(source: McCommandSource, arguments: Array<String>?): Boolean {
        return true
    }
}
