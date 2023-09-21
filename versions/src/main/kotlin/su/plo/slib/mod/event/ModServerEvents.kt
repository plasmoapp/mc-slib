package su.plo.slib.mod.event

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import su.plo.slib.api.server.event.command.ServerCommandsRegisterEvent
import su.plo.slib.api.event.player.PlayerJoinEvent
import su.plo.slib.api.event.player.PlayerQuitEvent
import su.plo.slib.mod.ModServerLib
import su.plo.slib.mod.event.server.ServerStartedEvent
import su.plo.slib.mod.event.server.ServerStoppingEvent
import su.plo.slib.mod.extension.toMcServerPlayer

//#if FABRIC

//#if MC>=11900
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
//#else
//$$ import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
//#endif

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import su.plo.slib.mod.channel.RegisterChannelHandler

//#else

//$$ import net.minecraftforge.event.RegisterCommandsEvent
//$$ import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent
//$$ import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent
//$$ import net.minecraftforge.eventbus.api.SubscribeEvent

//#endif

/**
 * todo: add this to readme
 *  make sure to create this instance AFTER channel initialization in [McChannelManager]
 */
class ModServerEvents {

    //#if FABRIC

    init {
        ServerLifecycleEvents.SERVER_STARTED.register { fireServerStarted(it) }
        ServerLifecycleEvents.SERVER_STOPPING.register { fireServerStopping(it) }

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ -> firePlayerJoin(handler.player) }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ -> firePlayerQuit(handler.player) }

        S2CPlayChannelEvents.REGISTER.register(RegisterChannelHandler)

        //#if MC>=11900
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> fireRegisterCommands(dispatcher) }
        //#else
        //$$ CommandRegistrationCallback.EVENT.register { dispatcher, _ -> fireRegisterCommands(dispatcher) }
        //#endif
    }

    //#else

    //$$ @SubscribeEvent
    //$$ fun onServerStart(event: net.minecraftforge.event.server.ServerStartedEvent) {
    //$$     fireServerStarted(event.server)
    //$$ }

    //$$ @SubscribeEvent
    //$$ fun onServerStart(event: net.minecraftforge.event.server.ServerStoppingEvent) {
    //$$     fireServerStopping(event.server)
    //$$ }

    //$$ @SubscribeEvent
    //$$ fun onPlayerJoin(event: PlayerLoggedInEvent) {
    //$$     val player = event.entity as? ServerPlayer ?: return
    //$$     firePlayerJoin(player)
    //$$ }

    //$$ @SubscribeEvent
    //$$ fun onPlayerQuit(event: PlayerLoggedOutEvent) {
    //$$     val player = event.entity as? ServerPlayer ?: return
    //$$     firePlayerQuit(player)
    //$$ }

    //$$ @SubscribeEvent
    //$$ fun onCommandRegister(event: RegisterCommandsEvent) {
    //$$     fireRegisterCommands(event.dispatcher)
    //$$ }

    //#endif

    private fun fireServerStarted(minecraftServer: MinecraftServer) {
        ServerStartedEvent.invoker.onServerStarted(minecraftServer)
    }

    private fun fireServerStopping(minecraftServer: MinecraftServer) {
        ServerStoppingEvent.invoker.onServerStopping(minecraftServer)
    }

    private fun fireRegisterCommands(dispatcher: CommandDispatcher<CommandSourceStack>) {
        ModServerLib.INSTANCE?.let {
            ServerCommandsRegisterEvent.invoker.onCommandsRegister(it.commandManager, it)
            it.commandManager.registerCommands(dispatcher)
        }
    }

    private fun firePlayerJoin(player: ServerPlayer) {
        player.toMcServerPlayer()?.let {
            PlayerJoinEvent.invoker.onPlayerJoin(it)
        }
    }

    private fun firePlayerQuit(player: ServerPlayer) {
        player.toMcServerPlayer()?.let {
            PlayerQuitEvent.invoker.onPlayerQuit(it)
        }
    }
}
