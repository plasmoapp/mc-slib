package su.plo.slib.command.brigadier

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.RedirectModifier
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import com.mojang.brigadier.tree.RootCommandNode
import su.plo.slib.api.command.brigadier.CustomArgumentType
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.logging.McLogger
import java.util.IdentityHashMap
import java.util.concurrent.atomic.AtomicBoolean

private val notBoundCommandException = SimpleCommandExceptionType(
    LiteralMessage("This command is not available yet, the server is still starting")
)

private val unboundCommandException = SimpleCommandExceptionType(
    LiteralMessage("This command is no longer available")
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

    val copy = builder.build()
    if (redirect != null) children.forEach { copy.addChild(it) }

    return copy
}

@Suppress("UNCHECKED_CAST")
fun <S, T> CommandContext<S>.copyFor(source: T): CommandContext<T> =
    (this as CommandContext<T>).copyFor(source)

fun <S> LiteralCommandNode<McBrigadierSource>.proxied(
    logger: McLogger,
    sourceFactory: (S) -> McBrigadierSource,
    contextFactory: (CommandContext<S>) -> CommandContext<McBrigadierSource>,
    argumentTypeMapper: (ArgumentType<*>) -> ArgumentType<*>? = { null },
    sourceUnwrapper: (McBrigadierSource) -> S = { it.getInstance() },
    isUnbound: () -> Boolean = { false },
): LiteralCommandNode<S> =
    CommandNodeProxy(logger, sourceFactory, contextFactory, argumentTypeMapper, sourceUnwrapper, isUnbound)
        .proxy(this) as LiteralCommandNode<S>

private class CommandNodeProxy<S>(
    private val logger: McLogger,
    private val sourceFactory: (S) -> McBrigadierSource,
    private val contextFactory: (CommandContext<S>) -> CommandContext<McBrigadierSource>,
    private val argumentTypeMapper: (ArgumentType<*>) -> ArgumentType<*>?,
    private val sourceUnwrapper: (McBrigadierSource) -> S,
    private val isUnbound: () -> Boolean,
) {
    private val proxies = IdentityHashMap<CommandNode<McBrigadierSource>, CommandNode<S>>()

    fun proxy(node: CommandNode<McBrigadierSource>): CommandNode<S> {
        proxies[node]?.let { return it }

        val redirect = node.redirect?.let { target ->
            require(target !is RootCommandNode) {
                "Command node '${node.name}' redirects to a root node, which can't be proxied"
            }
            proxy(target)
        }
        proxies[node]?.let { return it }

        val proxy = node.toProxyNode(redirect)
        proxies[node] = proxy

        node.children.forEach { proxy.addChild(proxy(it)) }

        return proxy
    }

    private fun CommandNode<McBrigadierSource>.toProxyNode(redirect: CommandNode<S>?): CommandNode<S> {
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

        if (redirect != null) {
            val modifier = redirectModifier
            val proxiedModifier = modifier?.let {
                RedirectModifier { context ->
                    if (isUnbound()) throw unboundCommandException.create()

                    val context = contextFactory(context)
                    modifier.apply(context).map(sourceUnwrapper)
                }
            }

            node.forward(redirect, proxiedModifier, isFork)
        }

        requirement?.let { requirement ->
            val warned = AtomicBoolean()

            node.requires { sourceStack ->
                if (isUnbound()) return@requires false

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
                if (isUnbound()) throw unboundCommandException.create()

                val context = contextFactory(context)
                if (!context.source.isBound) throw notBoundCommandException.create()

                try {
                    command.run(context)
                } catch (e: CommandSyntaxException) {
                    throw e.localizedFor(context.source)
                }
            }
        }

        if (this is ArgumentCommandNode<McBrigadierSource, *>) {
            val node = node as RequiredArgumentBuilder<S, *>
            if (this.customSuggestions != null) {
                node.suggests { context, builder ->
                    if (isUnbound()) return@suggests builder.buildFuture()

                    val context = contextFactory(context)
                    listSuggestions(context, builder)
                }
            }
        }

        if (mappedArgumentType == null && node is RequiredArgumentBuilder<S, *> && node.type is CustomArgumentType<*, *>) {
            @Suppress("UNCHECKED_CAST")
            return (node as RequiredArgumentBuilder<S, Any>).buildCustom<S, Any, Any>(sourceFactory)
        }

        return node.build()
    }
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
