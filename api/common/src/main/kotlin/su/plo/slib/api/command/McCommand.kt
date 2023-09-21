package su.plo.slib.api.command

import com.google.common.collect.ImmutableList

interface McCommand {

    /**
     * Invoked when [McCommandSource] executes the command
     */
    fun execute(source: McCommandSource, arguments: Array<String>)

    /**
     * @return list of suggestions
     */
    fun suggest(source: McCommandSource, arguments: Array<String>): List<String> {
        return ImmutableList.of()
    }

    /**
     * Note: Arguments are not null only on execution
     *
     * @return true if command source has permission to execute this command
     */
    fun hasPermission(source: McCommandSource, arguments: Array<String>?): Boolean {
        return true
    }
}
