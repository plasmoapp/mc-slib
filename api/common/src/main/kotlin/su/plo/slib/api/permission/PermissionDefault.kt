package su.plo.slib.api.permission

enum class PermissionDefault {
    TRUE,
    FALSE,
    OP,
    NOT_OP;

    fun getValue(op: Boolean): Boolean {
        return when (this) {
            TRUE -> true
            OP -> op
            NOT_OP -> !op
            else -> false
        }
    }
}
