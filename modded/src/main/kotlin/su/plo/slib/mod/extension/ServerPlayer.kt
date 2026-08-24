package su.plo.slib.mod.extension

import net.minecraft.server.level.ServerPlayer
import su.plo.slib.api.server.McServerLib
import su.plo.slib.mod.ModServerLib

fun ServerPlayer.toMcServerPlayer(minecraftServer: McServerLib = ModServerLib) =
    minecraftServer.getPlayerByInstance(this)
