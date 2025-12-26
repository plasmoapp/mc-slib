package su.plo.slib.minestom.command.brigadier

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import net.minestom.server.entity.Player
import net.minestom.server.utils.entity.EntityFinder
import su.plo.slib.api.command.brigadier.McBrigadierSource
import su.plo.slib.api.server.command.brigadier.McArgumentResolver
import su.plo.slib.api.server.command.brigadier.McArgumentTypes
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.minestom.MinestomServerLib

class MinestomEntityArguments : McArgumentTypes.Provider, McArgumentResolver.Provider {

    private val serverLib by lazy {
        MinestomServerLib.instance
    }

    @Suppress("UNCHECKED_CAST")
    override fun entity(): ArgumentType<Any> =
        MinestomArgumentType { name ->
            ArgumentEntity(name).singleEntity(true).onlyPlayers(false)
        } as ArgumentType<Any>

    @Suppress("UNCHECKED_CAST")
    override fun entities(): ArgumentType<Any> =
        MinestomArgumentType { name ->
            ArgumentEntity(name).singleEntity(false).onlyPlayers(false)
        } as ArgumentType<Any>

    @Suppress("UNCHECKED_CAST")
    override fun player(): ArgumentType<Any> =
        MinestomArgumentType { name ->
            ArgumentEntity(name).singleEntity(true).onlyPlayers(true)
        } as ArgumentType<Any>

    @Suppress("UNCHECKED_CAST")
    override fun players(): ArgumentType<Any> =
        MinestomArgumentType { name ->
            ArgumentEntity(name).singleEntity(false).onlyPlayers(true)
        } as ArgumentType<Any>

    override fun getEntity(context: CommandContext<McBrigadierSource>, name: String): McServerEntity {
        val finder = context.getArgument(name, EntityFinder::class.java)
        val brigadierContext = context.source as MinestomBrigadierSource

        val entity = finder.findFirstEntity(brigadierContext.getInstance())
            ?: throw IllegalArgumentException("No entity found")

        return serverLib.getEntityByInstance(entity)

    }

    override fun getEntities(context: CommandContext<McBrigadierSource>, name: String): Collection<McServerEntity> {
        val finder = context.getArgument(name, EntityFinder::class.java)
        val brigadierContext = context.source as MinestomBrigadierSource

        return finder.find(brigadierContext.getInstance()).map { serverLib.getEntityByInstance(it) }
    }

    override fun getPlayer(context: CommandContext<McBrigadierSource>, name: String): McServerPlayer {
        val finder = context.getArgument(name, EntityFinder::class.java)
        val brigadierContext = context.source as MinestomBrigadierSource

        val player = finder.findFirstPlayer(brigadierContext.getInstance())
            ?: throw IllegalArgumentException("No player found")

        return serverLib.getPlayerByInstance(player)
    }

    override fun getPlayers(context: CommandContext<McBrigadierSource>, name: String): Collection<McServerPlayer> {
        val finder = context.getArgument(name, EntityFinder::class.java)
        val brigadierContext = context.source as MinestomBrigadierSource

        return finder.find(brigadierContext.getInstance())
            .filterIsInstance<Player>()
            .map { serverLib.getPlayerByInstance(it) }
    }
}
