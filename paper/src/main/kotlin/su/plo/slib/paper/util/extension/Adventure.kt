package su.plo.slib.paper.util.extension

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.paper.PaperServerLib

fun McTextComponent.toAdventure(minecraftServer: PaperServerLib, source: McCommandSource): Component =
    GsonComponentSerializer.gson().deserialize(minecraftServer.textConverter.convertToJson(source, this))
