package su.plo.slib.api.permission

/**
 * Represents a tristate permission value that can be either TRUE, FALSE, or UNDEFINED.
 */
enum class PermissionTristate {
    /**
     * The permission is allowed.
     */
    TRUE,

    /**
     * The permission is denied.
     */
    FALSE,

    /**
     * The permission is unset or in an undefined state.
     */
    UNDEFINED;

    /**
     * Converts the tristate to a boolean value.
     *
     * If the tristate is UNDEFINED, the provided default value is returned.
     *
     * @param defaultValue The default boolean value to use if the tristate is UNDEFINED.
     * @return `true` if the tristate is TRUE, `false` if it is FALSE, or the `defaultValue` if it is UNDEFINED.
     */
    fun booleanValue(defaultValue: Boolean): Boolean {
        return if (this == UNDEFINED) defaultValue else this == TRUE
    }

    companion object {

        /**
         * Converts a boolean value to a [PermissionTristate].
         *
         * @param value The boolean value to convert.
         * @return The corresponding [PermissionTristate] value (TRUE, FALSE, or UNDEFINED).
         */
        @JvmStatic
        fun fromBoolean(value: Boolean?) = when (value) {
            true -> TRUE
            false -> FALSE
            else -> UNDEFINED
        }
    }
}
