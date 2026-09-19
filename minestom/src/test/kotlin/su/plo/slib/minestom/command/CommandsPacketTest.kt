package su.plo.slib.minestom.command

import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.network.NetworkBuffer
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertDoesNotThrow
import su.plo.slib.minestom.TestMinestomEnvironment

class CommandsPacketTest {
    private val arguments: List<Argument<*>>
        get() = TestMinestomEnvironment.commandManager.commands.flatMap { it.collectArguments() }

    @Test
    fun `commands are registered`() {
        assertFalse(arguments.isEmpty())
    }

    @TestFactory
    fun `argument nodes write properties the client can read back`(): List<DynamicTest> =
        arguments
            .filter { it.parser() != null }
            .map { argument ->
                DynamicTest.dynamicTest("${argument.id} (${argument.parser()})") {
                    val written = argument.nodeProperties() ?: ByteArray(0)

                    val node = DeclareCommandsPacket.Node().apply {
                        flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.ARGUMENT, true, false, false)
                        name = argument.id
                        parser = argument.parser()
                        properties = written
                    }

                    val bytes = NetworkBuffer.makeArray { it.write(DeclareCommandsPacket.Node.SERIALIZER, node) }
                    val buffer = NetworkBuffer.wrap(bytes, 0, bytes.size)
                    val read = assertDoesNotThrow("not decodable") { buffer.read(DeclareCommandsPacket.Node.SERIALIZER) }

                    assertArrayEquals(written, read.properties ?: ByteArray(0))
                }
            }

    private fun Command.collectArguments(): List<Argument<*>> =
        syntaxes.flatMap { it.arguments.asList() } + subcommands.flatMap { it.collectArguments() }
}
