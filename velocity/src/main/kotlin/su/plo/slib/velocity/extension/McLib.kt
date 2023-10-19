package su.plo.slib.velocity.extension

import su.plo.slib.api.McLib
import su.plo.slib.velocity.chat.ComponentTextConverter

fun McLib.textConverter() =
    this.textConverter as ComponentTextConverter
