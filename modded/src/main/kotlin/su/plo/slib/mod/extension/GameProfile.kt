package su.plo.slib.mod.extension

import com.mojang.authlib.GameProfile
import su.plo.slib.api.entity.player.McGameProfile

fun GameProfile.toMcGameProfile(): McGameProfile =
    McGameProfile(
        id,
        name,
        properties.values().map {
            McGameProfile.Property(it.name, it.value, it.signature)
        }
    )
