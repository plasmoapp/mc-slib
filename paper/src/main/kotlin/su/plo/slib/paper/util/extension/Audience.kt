package su.plo.slib.paper.util.extension

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.command.CommandSender
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.paper.PaperServerLib

fun CommandSender.sendMessage(minecraftServer: PaperServerLib, source: McCommandSource, text: McTextComponent) {
    sendMessage(minecraftServer.toComponent(source, text))
}

fun CommandSender.sendActionBar(minecraftServer: PaperServerLib, source: McCommandSource, text: McTextComponent) {
    sendActionBar(minecraftServer.toComponent(source, text))
}

private fun PaperServerLib.toComponent(source: McCommandSource, text: McTextComponent): Component =
    GsonComponentSerializer.gson().deserialize(textConverter.convertToJson(source, text))
