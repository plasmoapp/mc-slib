package su.plo.slib.minestom.command.brigadier

import com.mojang.brigadier.arguments.ArgumentType
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import su.plo.slib.api.server.command.brigadier.McArgumentTypes
import su.plo.slib.api.server.command.brigadier.McEntitiesArgumentResolver
import su.plo.slib.api.server.command.brigadier.McEntityArgumentResolver
import su.plo.slib.api.server.command.brigadier.McPlayerArgumentResolver
import su.plo.slib.api.server.command.brigadier.McPlayersArgumentResolver
import su.plo.slib.api.server.command.brigadier.ServerPos3dResolver
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.position.ServerPos3d
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

    override fun position(): ArgumentType<ServerPos3dResolver> =
        argumentResolver(
            MinestomArgumentType { name -> ArgumentRelativeBlockPosition(name) }
        ) { resolver ->
            ServerPos3dResolver { source ->
                val entity = source.executor as? McServerEntity
                val entityPosition = entity?.getServerPosition()

                val position = resolver.from(entity?.getInstance<Entity>())

                ServerPos3d(
                    entity?.world,
                    position.x(),
                    position.y(),
                    position.z(),
                    entityPosition?.yaw ?: 0f,
                    entityPosition?.pitch ?: 0f,
                )
            }
        }

    private fun <S, T> argumentResolver(
        finderArgumentType: MinestomArgumentType<S>,
        resolverFactory: (S) -> T,
    ): ArgumentType<T> =
        MinestomArgumentType { name ->
            finderArgumentType.argumentBuilder(name).map { resolverFactory(it) }
        }
}
