package su.plo.slib.mod.extension

import su.plo.slib.api.McLib
import su.plo.slib.mod.chat.ServerComponentTextConverter

fun McLib.textConverter() =
    this.textConverter as ServerComponentTextConverter
