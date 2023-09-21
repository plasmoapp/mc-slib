package su.plo.slib.api.permission

enum class PermissionTristate {
    TRUE,
    FALSE,
    UNDEFINED;

    fun booleanValue(defaultValue: Boolean): Boolean {
        return if (this == UNDEFINED) defaultValue else this == TRUE
    }

    companion object {

        fun fromBoolean(value: Boolean?) = when (value) {
            true -> TRUE
            false -> FALSE
            else -> UNDEFINED
        }
    }
}
