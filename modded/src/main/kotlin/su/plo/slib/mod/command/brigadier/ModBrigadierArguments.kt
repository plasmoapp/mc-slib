package su.plo.slib.mod.command.brigadier

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import su.plo.slib.api.command.brigadier.CustomArgumentType
import su.plo.slib.api.server.command.brigadier.McArgumentTypes
import su.plo.slib.api.server.command.brigadier.McEntitiesArgumentResolver
import su.plo.slib.api.server.command.brigadier.McEntityArgumentResolver
import su.plo.slib.api.server.command.brigadier.McPlayerArgumentResolver
import su.plo.slib.api.server.command.brigadier.McPlayersArgumentResolver
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

    private fun <T> argumentResolver(
        nativeType: ArgumentType<EntitySelector>,
        resolverFactory: (EntitySelector) -> T,
    ): CustomArgumentType<T, EntitySelector> =
        object : CustomArgumentType<T, EntitySelector> {
            override val nativeType = nativeType

            override fun parse(reader: StringReader) = resolverFactory(nativeType.parse(reader))
        }
}
