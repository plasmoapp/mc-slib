package su.plo.slib.mod.command.brigadier

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.coordinates.BlockPosArgument
import su.plo.slib.api.command.brigadier.CustomArgumentType
import su.plo.slib.api.server.command.brigadier.McArgumentTypes
import su.plo.slib.api.server.command.brigadier.McEntitiesArgumentResolver
import su.plo.slib.api.server.command.brigadier.McEntityArgumentResolver
import su.plo.slib.api.server.command.brigadier.McPlayerArgumentResolver
import su.plo.slib.api.server.command.brigadier.McPlayersArgumentResolver
import su.plo.slib.api.server.command.brigadier.ServerPos3dResolver
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.position.ServerPos3d
import su.plo.slib.mod.ModServerLib

class ModBrigadierArguments : McArgumentTypes.Provider {
    private val serverLib by lazy { ModServerLib }

    override fun entity(): ArgumentType<McEntityArgumentResolver> =
        argumentResolver(EntityArgument.entity()) { selector ->
            McEntityArgumentResolver { source ->
                serverLib.getEntityByInstance(selector.findSingleEntity(source.getInstance()))
            }
        }

    override fun entities(): ArgumentType<McEntitiesArgumentResolver> =
        argumentResolver(EntityArgument.entities()) { selector ->
            McEntitiesArgumentResolver { source ->
                selector.findEntities(source.getInstance()).map { serverLib.getEntityByInstance(it) }
            }
        }

    override fun player(): ArgumentType<McPlayerArgumentResolver> =
        argumentResolver(EntityArgument.player()) { selector ->
            McPlayerArgumentResolver { source ->
                serverLib.getPlayerByInstance(selector.findSinglePlayer(source.getInstance()))
            }
        }

    override fun players(): ArgumentType<McPlayersArgumentResolver> =
        argumentResolver(EntityArgument.players()) { selector ->
            McPlayersArgumentResolver { source ->
                selector.findPlayers(source.getInstance()).map { serverLib.getPlayerByInstance(it) }
            }
        }

    override fun position(): ArgumentType<ServerPos3dResolver> =
        argumentResolver(BlockPosArgument.blockPos()) { coordinates ->
            ServerPos3dResolver { source ->
                val stack = source.getInstance<CommandSourceStack>()
                val position = coordinates.getPosition(stack)
                val rotation = coordinates.getRotation(stack)

                val world = (source.executor as? McServerEntity)?.world

                ServerPos3d(
                    world,
                    position.x(),
                    position.y(),
                    position.z(),
                    rotation.y,
                    rotation.x,
                )
            }
        }

    private fun <S, T> argumentResolver(
        nativeType: ArgumentType<S>,
        resolverFactory: (S) -> T,
    ): CustomArgumentType<T, S> =
        object : CustomArgumentType<T, S> {
            override val nativeType = nativeType

            override fun parse(reader: StringReader) = resolverFactory(nativeType.parse(reader))
        }
}
