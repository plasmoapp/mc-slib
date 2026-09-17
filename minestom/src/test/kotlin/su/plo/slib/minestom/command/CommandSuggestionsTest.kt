package su.plo.slib.minestom.command

import net.minestom.server.listener.TabCompleteListener
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import su.plo.slib.minestom.TestMinestomEnvironment

class CommandSuggestionsTest {
    @ParameterizedTest
    @ValueSource(strings = ["/brigadier-nested-custom-type ", "/brigadier-nested-custom-type ev"])
    fun `suggestions are filtered by the word being completed`(input: String) {
        val sender = TestMinestomEnvironment.commandManager.consoleSender
        val entries = TabCompleteListener.getSuggestion(sender, input)?.entries?.map { it.entry }

        assertEquals(listOf("everyone"), entries)
    }
}
