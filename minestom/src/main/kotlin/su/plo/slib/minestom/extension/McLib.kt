package su.plo.slib.minestom.extension

import su.plo.slib.api.McLib
import su.plo.slib.chat.AdventureComponentTextConverter

fun McLib.textConverter() =
    this.textConverter as AdventureComponentTextConverter
