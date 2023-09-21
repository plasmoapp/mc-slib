package su.plo.slib.api.entity.player

import java.util.*

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
        val signature: String
    )
}
