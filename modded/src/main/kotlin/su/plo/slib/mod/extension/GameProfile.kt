package su.plo.slib.mod.extension

import com.mojang.authlib.GameProfile
import su.plo.slib.api.entity.player.McGameProfile

//? if >=1.21.9 {
/*import net.minecraft.server.players.NameAndId
*///?}

fun GameProfile.toMcGameProfile(): McGameProfile =
    McGameProfile(
        id,
        name,
        properties.values().map {
            McGameProfile.Property(it.name, it.value, it.signature)
        }
    )

//? if >=1.21.9 {
/*fun NameAndId.toMcGameProfile(): McGameProfile =
    McGameProfile(
        id,
        name,
        emptyList(),
    )
*///?}
