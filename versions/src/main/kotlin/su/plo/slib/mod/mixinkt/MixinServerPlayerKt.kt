package su.plo.slib.mod.mixinkt

import net.minecraft.network.protocol.game.ServerboundClientInformationPacket
import net.minecraft.server.level.ServerPlayer
import su.plo.slib.mod.entity.ModServerPlayer
import su.plo.slib.mod.extension.toMcServerPlayer

object MixinServerPlayerKt {

    fun updateOptions(player: ServerPlayer, packet: ServerboundClientInformationPacket) {
        val mcServerPlayer = player.toMcServerPlayer() ?: return

        var language = "en_us"

        val packetClass = packet::class.java
        packetClass.fields
            .filter { field -> field.type == String::class.java }
            .forEach { field ->
                language = field.get(packet) as String
            }
        (mcServerPlayer as ModServerPlayer).language = language
    }
}
