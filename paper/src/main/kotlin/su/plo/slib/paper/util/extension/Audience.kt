package su.plo.slib.paper.util.extension

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.command.CommandSender
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.paper.PaperServerLib

fun CommandSender.sendMessage(minecraftServer: PaperServerLib, text: McTextComponent) {
    val json = minecraftServer.textConverter.convertToJson(text)

    sendMessage(GsonComponentSerializer.gson().deserialize(json))
}

fun CommandSender.sendActionBar(minecraftServer: PaperServerLib, text: McTextComponent) {
    val json = minecraftServer.textConverter.convertToJson(text)

    sendActionBar(GsonComponentSerializer.gson().deserialize(json))
}
