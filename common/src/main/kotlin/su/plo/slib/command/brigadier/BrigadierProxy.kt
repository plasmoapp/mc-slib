package su.plo.slib.command.brigadier

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.RedirectModifier
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import su.plo.slib.api.command.brigadier.CustomArgumentType
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.logging.McLogger
import java.util.concurrent.atomic.AtomicBoolean

private val NotBoundCommandException = SimpleCommandExceptionType(
    LiteralMessage("This command is not available yet, the server is still starting")
)

fun <S> LiteralCommandNode<S>.copyLiteral(newLiteral: String): LiteralCommandNode<S> {
    val builder = LiteralArgumentBuilder.literal<S>(newLiteral)
        .requires(requirement)

    command?.let { builder.executes(it) }

    if (redirect != null) {
        builder.forward(redirect, redirectModifier, isFork)
    } else if (children.isNotEmpty()) {
        builder.redirect(this)
    }

    return builder.build()
}

@Suppress("UNCHECKED_CAST")
fun <S, T> CommandContext<S>.copyFor(source: T): CommandContext<T> =
    (this as CommandContext<T>).copyFor(source)

fun <S> LiteralCommandNode<McBrigadierSource>.proxied(
    logger: McLogger,
    sourceFactory: (S) -> McBrigadierSource,
    contextFactory: (CommandContext<S>) -> CommandContext<McBrigadierSource>,
    argumentTypeMapper: (ArgumentType<*>) -> ArgumentType<*>? = { null },
): LiteralCommandNode<S> =
    toProxyNode(logger, sourceFactory, contextFactory, argumentTypeMapper) as LiteralCommandNode<S>

private fun <S> CommandNode<McBrigadierSource>.toProxyNode(
    logger: McLogger,
    sourceFactory: (S) -> McBrigadierSource,
    contextFactory: (CommandContext<S>) -> CommandContext<McBrigadierSource>,
    argumentTypeMapper: (ArgumentType<*>) -> ArgumentType<*>? = { null },
): CommandNode<S> {
    var mappedArgumentType: ArgumentType<*>? = null

    val node =
        when (this) {
            is LiteralCommandNode -> LiteralArgumentBuilder.literal<S>(literal)
            is ArgumentCommandNode<McBrigadierSource, *> -> {
                mappedArgumentType = argumentTypeMapper(type)
                RequiredArgumentBuilder.argument(name, (mappedArgumentType ?: type) as ArgumentType<Any>)
            }
            else -> throw IllegalArgumentException("Unsupported command node: $this")
        }

    redirect?.let { redirect ->
        val modifier = redirectModifier

        if (modifier == null) {
            node.redirect(redirect.toProxyNode(logger, sourceFactory, contextFactory, argumentTypeMapper))
        } else {
            val proxiedModifier = RedirectModifier { context ->
                val context = contextFactory(context)
                modifier.apply(context).map { it.getInstance() }
            }

            node.fork(
                redirect.toProxyNode(logger, sourceFactory, contextFactory, argumentTypeMapper),
                proxiedModifier,
            )
        }
    }

    children
        .map { it.toProxyNode(logger, sourceFactory, contextFactory, argumentTypeMapper) }
        .forEach { node.then(it) }

    requirement?.let { requirement ->
        val warned = AtomicBoolean()

        node.requires { sourceStack ->
            val source = sourceFactory(sourceStack)

            if (source.isBound) {
                requirement.test(source)
            } else {
                try {
                    requirement.test(source)
                } catch (e: Throwable) {
                    if (warned.compareAndSet(false, true)) {
                        warnUnboundRequirementFailure(logger, this@toProxyNode, e)
                    }
                    true
                }
            }
        }
    }

    command?.let { command ->
        node.executes { context ->
            val context = contextFactory(context)
            if (!context.source.isBound) throw NotBoundCommandException.create()

            command.run(context)
        }
    }

    if (this is ArgumentCommandNode<McBrigadierSource, *>) {
        val node = node as RequiredArgumentBuilder<S, *>
        if (this.customSuggestions != null) {
            node.suggests { context, builder ->
                val context = contextFactory(context)
                listSuggestions(context, builder)
            }
        }
    }

    if (mappedArgumentType == null && node is RequiredArgumentBuilder<S, *> && node.type is CustomArgumentType<*, *>) {
        @Suppress("UNCHECKED_CAST")
        return (node as RequiredArgumentBuilder<S, Any>).buildCustom<S, Any, Any>()
    }

    return node.build()
}

private fun warnUnboundRequirementFailure(
    logger: McLogger,
    node: CommandNode<McBrigadierSource>,
    e: Throwable,
) {
    logger.warn(
        """
            Requirement of the command node '{}' threw before the server was initialized, treating the node as visible.
            Requirements run while datapack functions are parsed, so they must not depend on runtime state,
            check it inside executes instead
        """.trim().lines().joinToString(" "),
        node.name,
        e,
    )
}
