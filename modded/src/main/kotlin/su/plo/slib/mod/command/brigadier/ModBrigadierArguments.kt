package su.plo.slib.mod.command.brigadier

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.arguments.EntityArgument
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.server.command.brigadier.McArgumentResolver
import su.plo.slib.api.server.command.brigadier.McArgumentTypes
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.mod.ModServerLib
import su.plo.slib.mod.command.toSourceStack

@Suppress("UNCHECKED_CAST")
class ModBrigadierArguments : McArgumentTypes.Provider, McArgumentResolver.Provider {

    private val serverLib by lazy { ModServerLib }

    override fun entity(): ArgumentType<Any> = EntityArgument.entity() as ArgumentType<Any>

    override fun entities(): ArgumentType<Any> = EntityArgument.entities() as ArgumentType<Any>

    override fun player(): ArgumentType<Any> = EntityArgument.player() as ArgumentType<Any>

    override fun players(): ArgumentType<Any> = EntityArgument.players() as ArgumentType<Any>

    override fun getEntity(
        context: CommandContext<McBrigadierSource>,
        name: String,
    ): McServerEntity {
        val entity = EntityArgument.getEntity(context.toSourceStack(), name)
        return serverLib.getEntityByInstance(entity)
    }

    override fun getEntities(
        context: CommandContext<McBrigadierSource>,
        name: String,
    ): Collection<McServerEntity> {
        val entities = EntityArgument.getEntities(context.toSourceStack(), name)
        return entities.map { serverLib.getEntityByInstance(it) }
    }

    override fun getPlayer(
        context: CommandContext<McBrigadierSource>,
        name: String,
    ): McServerPlayer {
        val player = EntityArgument.getPlayer(context.toSourceStack(), name)
        return serverLib.getPlayerByInstance(player)
    }

    override fun getPlayers(
        context: CommandContext<McBrigadierSource>,
        name: String,
    ): Collection<McServerPlayer> {
        val players = EntityArgument.getPlayers(context.toSourceStack(), name)
        return players.map { serverLib.getPlayerByInstance(it) }
    }
}
