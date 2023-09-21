package su.plo.slib.mod.mixinkt

import net.minecraft.server.level.ServerPlayer
import su.plo.slib.mod.ModServerLib

object MixinPlayerListKt {

    fun onRespawn(newPlayer: ServerPlayer) {
        ModServerLib.INSTANCE?.getPlayerByInstance(newPlayer)
    }
}
