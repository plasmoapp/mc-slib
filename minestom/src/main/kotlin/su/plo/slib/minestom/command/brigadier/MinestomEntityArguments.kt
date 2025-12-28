package su.plo.slib.minestom.command.brigadier

import com.mojang.brigadier.arguments.ArgumentType
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import net.minestom.server.entity.Player
import net.minestom.server.utils.entity.EntityFinder
import su.plo.slib.api.server.command.brigadier.McArgumentTypes
import su.plo.slib.api.server.command.brigadier.McEntitiesArgumentResolver
import su.plo.slib.api.server.command.brigadier.McEntityArgumentResolver
import su.plo.slib.api.server.command.brigadier.McPlayerArgumentResolver
import su.plo.slib.api.server.command.brigadier.McPlayersArgumentResolver
import su.plo.slib.minestom.MinestomServerLib

class MinestomEntityArguments : McArgumentTypes.Provider {
    private val serverLib by lazy { MinestomServerLib.instance }

    override fun entity(): ArgumentType<McEntityArgumentResolver> =
        argumentResolver(
            MinestomArgumentType { name -> ArgumentEntity(name).singleEntity(true).onlyPlayers(false) }
        ) { finder ->
            McEntityArgumentResolver { source ->
                val entity = finder.findFirstEntity((source as MinestomBrigadierSource).getInstance())
                    ?: throw IllegalArgumentException("No entity found")
                serverLib.getEntityByInstance(entity)
            }
        }

    override fun entities(): ArgumentType<McEntitiesArgumentResolver> =
        argumentResolver(
            MinestomArgumentType { name -> ArgumentEntity(name).singleEntity(false).onlyPlayers(false) }
        ) { finder ->
            McEntitiesArgumentResolver { source ->
                finder.find((source as MinestomBrigadierSource).getInstance())
                    .map { serverLib.getEntityByInstance(it) }
            }
        }

    override fun player(): ArgumentType<McPlayerArgumentResolver> =
        argumentResolver(
            MinestomArgumentType { name -> ArgumentEntity(name).singleEntity(true).onlyPlayers(true) }
        ) { finder ->
            McPlayerArgumentResolver { source ->
                val player = finder.findFirstPlayer((source as MinestomBrigadierSource).getInstance())
                    ?: throw IllegalArgumentException("No player found")
                serverLib.getPlayerByInstance(player)
            }
        }

    override fun players(): ArgumentType<McPlayersArgumentResolver> =
        argumentResolver(
            MinestomArgumentType { name -> ArgumentEntity(name).singleEntity(false).onlyPlayers(true) }
        ) { finder ->
            McPlayersArgumentResolver { source ->
                finder.find((source as MinestomBrigadierSource).getInstance())
                    .filterIsInstance<Player>()
                    .map { serverLib.getPlayerByInstance(it) }
            }
        }

    private fun <T> argumentResolver(
        finderArgumentType: MinestomArgumentType<EntityFinder>,
        resolverFactory: (EntityFinder) -> T,
    ): ArgumentType<T> =
        MinestomArgumentType { name ->
            finderArgumentType.argumentBuilder(name).map { resolverFactory(it) }
        }
}
