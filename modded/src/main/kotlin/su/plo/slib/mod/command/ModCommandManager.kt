package su.plo.slib.mod.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.entity.player.Player
import su.plo.slib.api.command.McCommand
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.command.brigadier.McBrigadierRegistry
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.server.McServerLib
import su.plo.slib.command.AbstractCommandManager
import su.plo.slib.command.brigadier.applyEach
import su.plo.slib.command.brigadier.collectBrigadierCommands
import su.plo.slib.command.brigadier.copyLiteral
import su.plo.slib.command.brigadier.copyFor
import su.plo.slib.command.brigadier.proxied
import su.plo.slib.mod.command.brigadier.ModBrigadierSource
import su.plo.slib.mod.mixin.accessor.CommandSourceStackAccessor

//? if >=1.21.10 {
/*import su.plo.slib.mod.mixin.accessor.ServerPlayerCommandSourceAccessor
*///?}

class ModCommandManager(
    private val minecraftServer: McServerLib
) : AbstractCommandManager<McCommand>(minecraftServer.baseLogger) {

    @Synchronized
    fun registerCommands(
        dispatcher: CommandDispatcher<CommandSourceStack>,
        phase: McBrigadierRegistry.Phase,
    ) {
        registerCommands { name, command, _ ->
            val modCommand = ModCommand(minecraftServer, this, command)
            modCommand.register(dispatcher, name)
        }

        collectBrigadierCommands(phase)
            .applyEach(logger, logRegisteredCommands) { (node, _, aliases) ->
                val proxied = node.proxied(
                    logger,
                    ModBrigadierSource::from,
                    { it.toMc() },
                )

                dispatcher.root.addChild(proxied)
                aliases.forEach { alias -> dispatcher.root.addChild(proxied.copyLiteral(alias)) }
            }

        this.registered = true
    }

    override fun getCommandSource(sourceStack: Any): McCommandSource {
        require(sourceStack is CommandSourceStack) { "source is not " + CommandSourceStack::class.java }
        require(sourceStack is CommandSourceStackAccessor) { "source is not " + CommandSourceStackAccessor::class.java }

        val source = sourceStack.slib_getSource()

        //? if >=1.21.10 {
        /*val player = (source as? ServerPlayerCommandSourceAccessor)
            ?.let { minecraftServer.getPlayerByInstance(it.slib_getServerPlayer()) }
        *///?} else {
        val player = (source as? Player)?.let { minecraftServer.getPlayerByInstance(it) }
        //?}

        return player ?: ModDefaultCommandSource(minecraftServer, sourceStack)
    }
}

fun CommandContext<McBrigadierSource>.toSourceStack(): CommandContext<CommandSourceStack> =
    copyFor(source.getInstance() as CommandSourceStack)

fun CommandContext<CommandSourceStack>.toMc(): CommandContext<McBrigadierSource> =
    copyFor(ModBrigadierSource.from(source))

