package su.plo.slib.mod.command

import net.minecraft.commands.CommandSourceStack
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.permission.PermissionTristate
import su.plo.slib.mod.extension.textConverter

class ModDefaultCommandSource(
    private val minecraftServer: McServerLib,
    private val source: CommandSourceStack
) : McCommandSource {

    override val language: String
        get() = "en_us"

    override fun sendMessage(text: McTextComponent) {
        //#if MC>=11900
        source.sendSystemMessage(minecraftServer.textConverter().convert(this, text))
        //#else
        //$$ source.sendSuccess(minecraftServer.textConverter().convert(this, text), true);
        //#endif
    }

    override fun sendActionBar(text: McTextComponent) =
        sendMessage(text)

    override fun hasPermission(permission: String) =
        true

    override fun getPermission(permission: String) =
        PermissionTristate.FALSE
}
