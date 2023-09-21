package su.plo.slib.spigot.extension

import su.plo.slib.api.McLib
import su.plo.slib.spigot.chat.BaseComponentTextConverter

fun McLib.textConverter() =
    this.textConverter as BaseComponentTextConverter
