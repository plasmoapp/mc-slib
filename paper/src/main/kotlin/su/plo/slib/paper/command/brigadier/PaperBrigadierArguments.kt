@file:Suppress("UnstableApiUsage")

package su.plo.slib.paper.command.brigadier

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.converter.MessageTextConverter
import su.plo.slib.api.command.brigadier.CustomArgumentType
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.slib.api.server.command.brigadier.McArgumentTypes
import su.plo.slib.api.server.command.brigadier.McEntitiesArgumentResolver
import su.plo.slib.api.server.command.brigadier.McEntityArgumentResolver
import su.plo.slib.api.server.command.brigadier.McGameProfilesArgumentResolver
import su.plo.slib.api.server.command.brigadier.McPlayerArgumentResolver
import su.plo.slib.api.server.command.brigadier.McPlayersArgumentResolver
import su.plo.slib.api.server.command.brigadier.ServerPos3dResolver
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.position.ServerPos3d
import su.plo.slib.paper.PaperServerLib

private val errorUnknownPlayer = SimpleCommandExceptionType(
    MessageTextConverter.converter().convert(
        McTextComponent.translatable("argument.player.unknown")
    )
)

class PaperBrigadierArguments : McArgumentTypes.Provider {
    private val serverLib
        get() = PaperServerLib.instance

    override fun entity(): ArgumentType<McEntityArgumentResolver> =
        argumentResolver(ArgumentTypes.entity()) { selector ->
            McEntityArgumentResolver { source ->
                serverLib.getEntityByInstance(selector.resolve(source.getInstance()).first())
            }
        }

    override fun entities(): ArgumentType<McEntitiesArgumentResolver> =
        argumentResolver(ArgumentTypes.entities()) { selector ->
            McEntitiesArgumentResolver { source ->
                selector.resolve(source.getInstance()).map { serverLib.getEntityByInstance(it) }
            }
        }

    override fun player(): ArgumentType<McPlayerArgumentResolver> =
        argumentResolver(ArgumentTypes.player()) { selector ->
            McPlayerArgumentResolver { source ->
                serverLib.getPlayerByInstance(selector.resolve(source.getInstance()).first())
            }
        }

    override fun players(): ArgumentType<McPlayersArgumentResolver> =
        argumentResolver(ArgumentTypes.players()) { selector ->
            McPlayersArgumentResolver { source ->
                selector.resolve(source.getInstance()).map { serverLib.getPlayerByInstance(it) }
            }
        }

    override fun gameProfiles(): ArgumentType<McGameProfilesArgumentResolver> =
        argumentResolver(ArgumentTypes.playerProfiles()) { selector ->
            McGameProfilesArgumentResolver { source ->
                selector.resolve(source.getInstance()).map { profile ->
                    McGameProfile(
                        profile.id ?: throw errorUnknownPlayer.create(),
                        profile.name ?: throw errorUnknownPlayer.create(),
                        emptyList(),
                    )
                }
            }
        }

    override fun position(): ArgumentType<ServerPos3dResolver> =
        argumentResolver(ArgumentTypes.blockPosition()) { coordinates ->
            ServerPos3dResolver { source ->
                val sourceStack = source.getInstance<CommandSourceStack>()
                val position = coordinates.resolve(sourceStack)
                val location = sourceStack.location

                val world = location.world?.let { serverLib.getWorld(it) }
                    ?: (source.executor as? McServerEntity)?.world

                ServerPos3d(
                    world,
                    position.x(),
                    position.y(),
                    position.z(),
                    location.yaw,
                    location.pitch,
                )
            }
        }

    private fun <T, N> argumentResolver(
        nativeType: ArgumentType<N>,
        resolverFactory: (N) -> T,
    ): CustomArgumentType<T, N> =
        object : CustomArgumentType<T, N> {
            override val nativeType = nativeType

            override fun parse(reader: StringReader) = resolverFactory(nativeType.parse(reader))
        }
}
