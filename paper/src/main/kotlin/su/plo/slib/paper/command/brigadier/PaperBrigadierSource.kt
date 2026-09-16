@file:Suppress("UnstableApiUsage")

package su.plo.slib.paper.command.brigadier

import com.mojang.brigadier.exceptions.CommandSyntaxException
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.command.brigadier.McTextMessage
import su.plo.slib.api.entity.McEntity
import su.plo.slib.paper.PaperServerLib
import su.plo.slib.paper.command.PaperUnboundCommandSource

class PaperBrigadierSource(
    override val source: McCommandSource,
    override val executor: McEntity?,
    override val isBound: Boolean,
    private val instance: CommandSourceStack,
) : McBrigadierSource {
    override val isSilent: Boolean by lazy { instance.isSilent() }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getInstance(): T =
        instance as T

    override fun sendFailure(exception: CommandSyntaxException) {
        val rawMessage = exception.rawMessage
        if (rawMessage is McTextMessage) return super.sendFailure(exception)
        if (isSilent) return

        val message = MessageComponentSerializer.message().deserialize(rawMessage)
        instance.sender.sendMessage(Component.empty().append(message).color(NamedTextColor.RED))
    }

    companion object {
        fun from(sourceStack: CommandSourceStack): PaperBrigadierSource {
            val minecraftServer = PaperServerLib.instanceOrNull
                ?: return PaperBrigadierSource(
                    PaperUnboundCommandSource,
                    null,
                    false,
                    sourceStack,
                )

            return PaperBrigadierSource(
                minecraftServer.commandManager.getCommandSource(sourceStack.sender),
                sourceStack.executor?.let { minecraftServer.getEntityByInstance(it) },
                true,
                sourceStack,
            )
        }
    }
}

@Volatile
private var silentResolver: ((CommandSourceStack) -> Boolean)? = null

private fun CommandSourceStack.isSilent(): Boolean {
    val resolver = silentResolver
        ?: resolveSilent(javaClass).also { silentResolver = it }

    return resolver(this)
}

private fun resolveSilent(sourceStackClass: Class<*>): (CommandSourceStack) -> Boolean {
    runCatching { sourceStackClass.getMethod("isSilent") }
        .getOrNull()
        ?.takeIf { it.returnType == Boolean::class.javaPrimitiveType }
        ?.also { it.isAccessible = true }
        ?.let { method -> return { method.invoke(it) as Boolean } }

    generateSequence(sourceStackClass) { it.superclass }
        .mapNotNull { runCatching { it.getDeclaredField("silent") }.getOrNull() }
        .firstOrNull { it.type == Boolean::class.javaPrimitiveType }
        ?.also { it.isAccessible = true }
        ?.let { field -> return { field.getBoolean(it) } }

    return { _ -> false }
}
