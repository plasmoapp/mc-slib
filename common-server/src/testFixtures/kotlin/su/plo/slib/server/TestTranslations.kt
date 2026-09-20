package su.plo.slib.server

const val TEST_LANGUAGE = "en_us"

const val UNTRANSLATED_LANGUAGE = "zz_zz"

const val INVALID_ARGUMENT_KEY = "slibtest.argument.invalid"
const val FAILED_COMMAND_KEY = "slibtest.command.failed"
const val FEEDBACK_COMMAND_KEY = "slibtest.command.feedback"

val testTranslations = mapOf(
    INVALID_ARGUMENT_KEY to "slib argument error: %s",
    FAILED_COMMAND_KEY to "slib execute error: %s",
    FEEDBACK_COMMAND_KEY to "slib feedback: %s",
)
