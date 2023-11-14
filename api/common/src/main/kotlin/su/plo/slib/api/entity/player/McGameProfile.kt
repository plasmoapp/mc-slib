package su.plo.slib.api.entity.player

import java.util.*

/**
 * Represents a player's Minecraft game profile.
 *
 * This data class stores information about a player's profile: their UUID, username,
 * and associated properties.
 *
 * @param id          The unique identifier of the player.
 * @param name        The player's username.
 * @param properties  A list of properties associated with the player's profile.
 */
data class McGameProfile(
    val id: UUID,
    val name: String,
    val properties: List<Property>
) {

    override fun hashCode(): Int {
        return Objects.hash(id, name)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as McGameProfile

        if (id != other.id) return false
        return name == other.name
    }

    data class Property(
        val name: String,
        val value: String,
        val signature: String?
    )
}
