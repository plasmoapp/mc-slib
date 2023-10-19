package su.plo.slib.bungee.extension

import su.plo.slib.api.McLib
import su.plo.slib.bungee.chat.BaseComponentTextConverter

fun McLib.textConverter() =
    this.textConverter as BaseComponentTextConverter
