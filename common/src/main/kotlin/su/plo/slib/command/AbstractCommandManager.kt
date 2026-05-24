package su.plo.slib.command

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Maps
import com.mojang.brigadier.RedirectModifier
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandManager
import su.plo.slib.api.command.brigadier.CustomArgumentType
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.logging.McLogger
import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.command.brigadier.buildCustom

abstract class AbstractCommandManager<T : McCommand>(
    baseLogger: McLogger
) : McCommandManager<T>() {
    private val logger = McLoggerFactory.createLogger(baseLogger, "CommandManager")

    protected val commandByName: MutableMap<String, T> = Maps.newHashMap()
    protected val namespaceByName: MutableMap<String, String> = Maps.newHashMap()
    protected var brigadierCommands: MutableList<LiteralCommandNode<McBrigadierSource>> = mutableListOf()
    protected val brigadierNamespaceByLiteral: MutableMap<String, String> = Maps.newHashMap()

    protected var registered = false

    @get:Synchronized
    override val registeredCommands: Map<String, McCommand>
        get() = ImmutableMap.copyOf<String, McCommand>(commandByName)

    @get:Synchronized
    override val registeredBrigadierCommands: List<LiteralCommandNode<McBrigadierSource>>
        get() = ImmutableList.copyOf(brigadierCommands)

    @Synchronized
    override fun register(command: LiteralCommandNode<McBrigadierSource>) {
        check(!registered) { "register after commands registration is not supported" }
        require(brigadierCommands.none { it.literal == command.literal }) { "Command with name '${command.literal}' already exist" }

        brigadierCommands.add(command)
    }

    @Synchronized
    override fun register(namespace: String, command: LiteralCommandNode<McBrigadierSource>) {
        register(command)
        brigadierNamespaceByLiteral[command.literal] = namespace
    }

    @Synchronized
    override fun register(name: String, command: T, vararg aliases: String) {
        register(commandNamespace, name, command, *aliases)
    }

    @Synchronized
    override fun register(namespace: String, name: String, command: T, vararg aliases: String) {
        check(!registered) { "register after commands registration is not supported" }
        require(!commandByName.containsKey(name)) { "Command with name '$name' already exist" }

        for (alias in aliases) {
            require(!commandByName.containsKey(alias)) { "Command with name '$alias' already exist" }
        }

        commandByName[name] = command
        namespaceByName[name] = namespace
        for (alias in aliases) {
            commandByName[alias] = command
            namespaceByName[alias] = namespace
        }
    }

    @Synchronized
    override fun clear() {
        commandByName.clear()
        namespaceByName.clear()
        brigadierCommands.clear()
        brigadierNamespaceByLiteral.clear()
        registered = false
    }

    protected fun registerCommands(register: (String, T, String) -> Unit) {
        commandByName.forEach { (name, command) ->
            val namespace = namespaceByName[name] ?: commandNamespace
            register(name, command, namespace)
            logger.info("Command '$name' registered")
        }
    }

    protected fun registerBrigadierCommands(registerCommand: (LiteralCommandNode<McBrigadierSource>, String) -> Unit) {
        brigadierCommands.forEach { command ->
            val namespace = brigadierNamespaceByLiteral[command.literal] ?: commandNamespace
            registerCommand(command, namespace)
            logger.info("Command '${command.literal}' registered")
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <S, T> CommandContext<S>.copyFor(source: T): CommandContext<T> =
    (this as CommandContext<T>).copyFor(source)

fun <S> LiteralCommandNode<McBrigadierSource>.proxied(
    sourceFactory: (S) -> McBrigadierSource,
    contextFactory: (CommandContext<S>) -> CommandContext<McBrigadierSource>,
): LiteralCommandNode<S> =
    toProxyNode(sourceFactory, contextFactory) as LiteralCommandNode<S>

fun <S> CommandNode<McBrigadierSource>.toProxyNode(
    sourceFactory: (S) -> McBrigadierSource,
    contextFactory: (CommandContext<S>) -> CommandContext<McBrigadierSource>,
): CommandNode<S> {
    val node =
        when (this) {
            is LiteralCommandNode -> LiteralArgumentBuilder.literal<S>(literal)
            is ArgumentCommandNode<McBrigadierSource, *> ->
                RequiredArgumentBuilder.argument(name, type as ArgumentType<Any>)
            else -> throw IllegalArgumentException("Unsupported command node: $this")
        }

    redirect?.let { redirect ->
        val modifier = redirectModifier

        if (modifier == null) {
            node.redirect(redirect.toProxyNode(sourceFactory, contextFactory))
        } else {
            val proxiedModifier = RedirectModifier { context ->
                val context = contextFactory(context)
                modifier.apply(context).map { it.getInstance() }
            }

            node.fork(
                redirect.toProxyNode(sourceFactory, contextFactory),
                proxiedModifier,
            )
        }
    }

    children
        .map { it.toProxyNode(sourceFactory, contextFactory) }
        .forEach { node.then(it) }

    requirement?.let { requirement ->
        node.requires { sourceStack ->
            val source = sourceFactory(sourceStack)
            requirement.test(source)
        }
    }

    command?.let { command ->
        node.executes { context ->
            val context = contextFactory(context)
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

    if (node is RequiredArgumentBuilder<S, *> && node.type is CustomArgumentType<*, *>) {
        @Suppress("UNCHECKED_CAST")
        return (node as RequiredArgumentBuilder<S, Any>).buildCustom<S, Any, Any>()
    }

    return node.build()
}
