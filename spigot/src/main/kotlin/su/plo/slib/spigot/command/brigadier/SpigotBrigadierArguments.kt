package su.plo.slib.spigot.command.brigadier

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import su.plo.slib.api.server.command.brigadier.McArgumentResolver
import su.plo.slib.api.server.command.brigadier.McArgumentTypes
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.spigot.SpigotServerLib
import su.plo.slib.spigot.nms.ReflectionProxies
import java.lang.reflect.UndeclaredThrowableException

class SpigotBrigadierArguments: McArgumentTypes.Provider, McArgumentResolver.Provider {

    private val serverLib by lazy { SpigotServerLib.instance }

    override fun entity(): ArgumentType<Any> = ReflectionProxies.entityArgument.entity()

    override fun entities(): ArgumentType<Any> = ReflectionProxies.entityArgument.entities()

    override fun player(): ArgumentType<Any> = ReflectionProxies.entityArgument.players()

    override fun players(): ArgumentType<Any> = ReflectionProxies.entityArgument.players()

    override fun <S> getEntity(context: CommandContext<S>, name: String): McServerEntity {
        val entity = rethrowProxyException {
            ReflectionProxies.entityArgument.getEntity(context, name)
        }

        val bukkitEntity = ReflectionProxies.entity.getBukkitEntity(entity)
        return serverLib.getEntityByInstance(bukkitEntity)
    }

    override fun <S> getEntities(context: CommandContext<S>, name: String): Collection<McServerEntity> {
        val entities = rethrowProxyException {
            ReflectionProxies.entityArgument.getEntities(context, name)
        }

        return entities.map { entity ->
            val bukkitEntity = ReflectionProxies.entity.getBukkitEntity(entity)
            serverLib.getEntityByInstance(bukkitEntity)
        }
    }

    override fun <S> getPlayer(context: CommandContext<S>, name: String): McServerPlayer {
        val player = rethrowProxyException {
            ReflectionProxies.entityArgument.getPlayer(context, name)
        }

        val bukkitPlayer = ReflectionProxies.entity.getBukkitEntity(player)
        return serverLib.getPlayerByInstance(bukkitPlayer)
    }

    override fun <S> getPlayers(context: CommandContext<S>, name: String): Collection<McServerPlayer> {
        val players = rethrowProxyException {
            ReflectionProxies.entityArgument.getPlayers(context, name)
        }

        return players.map { player ->
            val bukkitPlayer = ReflectionProxies.entity.getBukkitEntity(player)
            serverLib.getPlayerByInstance(bukkitPlayer)
        }
    }

    private fun <T> rethrowProxyException(block: () -> T): T {
        try {
            return block()
        } catch (e: UndeclaredThrowableException) {
            throw e.undeclaredThrowable
        }
    }
}
