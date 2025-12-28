package su.plo.slib.spigot.command.brigadier

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import su.plo.slib.api.command.brigadier.CustomArgumentType
import su.plo.slib.api.server.command.brigadier.McArgumentTypes
import su.plo.slib.api.server.command.brigadier.McEntitiesArgumentResolver
import su.plo.slib.api.server.command.brigadier.McEntityArgumentResolver
import su.plo.slib.api.server.command.brigadier.McPlayerArgumentResolver
import su.plo.slib.api.server.command.brigadier.McPlayersArgumentResolver
import su.plo.slib.spigot.SpigotServerLib
import su.plo.slib.spigot.nms.ReflectionProxies
import java.lang.reflect.UndeclaredThrowableException

class SpigotBrigadierArguments: McArgumentTypes.Provider {
    private val serverLib by lazy { SpigotServerLib.instance }

    override fun entity(): ArgumentType<McEntityArgumentResolver> =
        argumentResolver(ReflectionProxies.entityArgument.entity()) { selector ->
            McEntityArgumentResolver { source ->
                val entity = rethrowProxyException {
                    ReflectionProxies.entitySelector.findSingleEntity(selector, source.getInstance())
                }
                val bukkitEntity = ReflectionProxies.entity.getBukkitEntity(entity)

                serverLib.getEntityByInstance(bukkitEntity)
            }
        }

    override fun entities(): ArgumentType<McEntitiesArgumentResolver> =
        argumentResolver(ReflectionProxies.entityArgument.entities()) { selector ->
            McEntitiesArgumentResolver { source ->
                val entities = rethrowProxyException {
                    ReflectionProxies.entitySelector.findEntities(selector, source.getInstance())
                }
                entities.map {
                    val bukkitEntity = ReflectionProxies.entity.getBukkitEntity(it)
                    serverLib.getEntityByInstance(bukkitEntity)
                }
            }
        }

    override fun player(): ArgumentType<McPlayerArgumentResolver> =
        argumentResolver(ReflectionProxies.entityArgument.player()) { selector ->
            McPlayerArgumentResolver { source ->
                val player = rethrowProxyException {
                    ReflectionProxies.entitySelector.findSinglePlayer(selector, source.getInstance())
                }
                val bukkitPlayer = ReflectionProxies.entity.getBukkitEntity(player)
                serverLib.getPlayerByInstance(bukkitPlayer)
            }
        }

    override fun players(): ArgumentType<McPlayersArgumentResolver> =
        argumentResolver(ReflectionProxies.entityArgument.players()) { selector ->
            McPlayersArgumentResolver { source ->
                val players = rethrowProxyException {
                    ReflectionProxies.entitySelector.findPlayers(selector, source.getInstance())
                }
                players.map {
                    val bukkitPlayer = ReflectionProxies.entity.getBukkitEntity(it)
                    serverLib.getPlayerByInstance(bukkitPlayer)
                }
            }
        }

    private fun <T> argumentResolver(
        nativeType: ArgumentType<Any>,
        resolverFactory: (Any) -> T,
    ): CustomArgumentType<T, Any> =
        object : CustomArgumentType<T, Any> {
            override val nativeType = nativeType

            override fun parse(reader: StringReader) = resolverFactory(nativeType.parse(reader))
        }

    private fun <T> rethrowProxyException(block: () -> T): T {
        try {
            return block()
        } catch (e: UndeclaredThrowableException) {
            throw e.undeclaredThrowable
        }
    }
}
