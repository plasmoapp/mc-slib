package su.plo.slib.mod.extension

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import su.plo.slib.api.server.McServerLib
import su.plo.slib.mod.ModServerLib

fun ServerPlayer.serverLevel() =
    this.level as ServerLevel

fun ServerPlayer.mustToMcServerPlayer(minecraftServer: McServerLib? = null) =
    minecraftServer?.getPlayerByInstance(this)
        ?: ModServerLib.INSTANCE?.getPlayerByInstance(this)
        ?: throw IllegalStateException("McServerLib is not initialized yet")

fun ServerPlayer.toMcServerPlayer(minecraftServer: McServerLib? = null) =
    minecraftServer?.getPlayerByInstance(this)
        ?: ModServerLib.INSTANCE?.getPlayerByInstance(this)
