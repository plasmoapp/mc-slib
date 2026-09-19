package su.plo.slib.paper.extension

import com.destroystokyo.paper.profile.PlayerProfile
import su.plo.slib.api.entity.player.McGameProfile

fun PlayerProfile.toMcGameProfileOrNull(): McGameProfile? {
    val id = id ?: return null
    val name = name?.takeIf { it.isNotEmpty() } ?: return null

    return McGameProfile(
        id,
        name,
        properties.map { McGameProfile.Property(it.name, it.value, it.signature) },
    )
}
