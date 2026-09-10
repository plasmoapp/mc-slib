package su.plo.slib.server

const val TEST_LANGUAGE = "en_us"

const val INVALID_ARGUMENT_KEY = "slibtest.argument.invalid"
const val FAILED_COMMAND_KEY = "slibtest.command.failed"

val testTranslations = mapOf(
    INVALID_ARGUMENT_KEY to "slib argument error: %s",
    FAILED_COMMAND_KEY to "slib execute error: %s",
)
