package su.plo.slib.spigot.command.brigadier

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
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
import su.plo.slib.spigot.SpigotServerLib
import su.plo.slib.spigot.nms.ReflectionProxies
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.UndeclaredThrowableException
import java.util.UUID

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

    override fun gameProfiles(): ArgumentType<McGameProfilesArgumentResolver> =
        argumentResolver(ReflectionProxies.gameProfileArgument.gameProfile()) { selector ->
            McGameProfilesArgumentResolver { source ->
                val getNamesMethod = selector.javaClass.declaredMethods
                    .first()
                    .also { it.isAccessible = true }

                @Suppress("UNCHECKED_CAST")
                val names = rethrowProxyException {
                    getNamesMethod.invoke(selector, source.getInstance()) as Collection<Any>
                }

                names.map {
                    // 1.21.9+ uses NameAndId, so we can't just use GameProfile here
                    val uuidMethod = it.javaClass.declaredMethods
                        .first { it.returnType == UUID::class.java }
                        .also { it.isAccessible = true }
                    val nameMethod = it.javaClass.declaredMethods
                        .first { it.returnType == String::class.java }
                        .also { it.isAccessible = true }

                    val uuid = uuidMethod.invoke(it) as UUID
                    val name = nameMethod.invoke(it) as String

                    McGameProfile(uuid, name, emptyList())
                }
            }
        }

    override fun position(): ArgumentType<ServerPos3dResolver> =
        argumentResolver(ReflectionProxies.blockPosArgument.blockPos()) { coordinates ->
            ServerPos3dResolver { source ->
                val position = ReflectionProxies.coordinates.getPosition(coordinates, source.getInstance())
                val rotation = ReflectionProxies.coordinates.getRotation(coordinates, source.getInstance())

                val world = (source.executor as? McServerEntity)?.world

                ServerPos3d(
                    world,
                    ReflectionProxies.vec3Proxy.x(position),
                    ReflectionProxies.vec3Proxy.y(position),
                    ReflectionProxies.vec3Proxy.z(position),
                    ReflectionProxies.vec2Proxy.y(rotation),
                    ReflectionProxies.vec2Proxy.x(rotation),
                )
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
        } catch (e: InvocationTargetException) {
            throw e.targetException
        } catch (e: UndeclaredThrowableException) {
            throw e.undeclaredThrowable
        }
    }
}
