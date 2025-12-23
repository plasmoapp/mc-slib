package su.plo.slib.mod.command.brigadier

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import su.plo.slib.api.server.command.brigadier.McArgumentResolver
import su.plo.slib.api.server.command.brigadier.McArgumentTypes
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.mod.ModServerLib

@Suppress("UNCHECKED_CAST")
class ModBrigadierArguments : McArgumentTypes.Provider, McArgumentResolver.Provider {

    private val serverLib by lazy { ModServerLib }

    override fun entity(): ArgumentType<Any> = EntityArgument.entity() as ArgumentType<Any>

    override fun entities(): ArgumentType<Any> = EntityArgument.entities() as ArgumentType<Any>

    override fun player(): ArgumentType<Any> = EntityArgument.player() as ArgumentType<Any>

    override fun players(): ArgumentType<Any> = EntityArgument.players() as ArgumentType<Any>

    override fun <S> getEntity(
        context: CommandContext<S>,
        name: String,
    ): McServerEntity {
        val entity = EntityArgument.getEntity(context as CommandContext<CommandSourceStack>, name)
        return serverLib.getEntityByInstance(entity)
    }

    override fun <S> getEntities(
        context: CommandContext<S>,
        name: String,
    ): Collection<McServerEntity> {
        val entities = EntityArgument.getEntities(context as CommandContext<CommandSourceStack>, name)
        return entities.map { serverLib.getEntityByInstance(it) }
    }

    override fun <S> getPlayer(
        context: CommandContext<S>,
        name: String,
    ): McServerPlayer {
        val player = EntityArgument.getPlayer(context as CommandContext<CommandSourceStack>, name)
        return serverLib.getPlayerByInstance(player)
    }

    override fun <S> getPlayers(
        context: CommandContext<S>,
        name: String,
    ): Collection<McServerPlayer> {
        val players = EntityArgument.getPlayers(context as CommandContext<CommandSourceStack>, name)
        return players.map { serverLib.getPlayerByInstance(it) }
    }
}
