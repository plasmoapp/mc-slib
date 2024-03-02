package su.plo.slib.minestom.extension

import su.plo.slib.api.McLib
import su.plo.slib.minestom.chat.BaseComponentTextConverter

fun McLib.textConverter() =
    this.textConverter as BaseComponentTextConverter
