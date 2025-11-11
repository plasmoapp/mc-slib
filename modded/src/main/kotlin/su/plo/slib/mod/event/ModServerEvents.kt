package su.plo.slib.mod.event

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent
import su.plo.slib.api.event.player.McPlayerJoinEvent
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.slib.mod.ModServerLib
import su.plo.slib.mod.channel.RegisterChannelHandler
import su.plo.slib.mod.event.server.ServerStartedEvent
import su.plo.slib.mod.event.server.ServerStoppingEvent
import su.plo.slib.mod.extension.toMcServerPlayer

//? if fabric {
//? if >=1.19 {
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
//?} else {
/*import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
*///?}
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
//? if >=1.20.2 {
/*import net.fabricmc.fabric.api.networking.v1.S2CConfigurationChannelEvents
*///?}
//?} elif forge {
/*import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
*///?} elif neoforge {
/*import net.neoforged.neoforge.common.NeoForge
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent
*///?}

/**
 * todo: add this to readme
 *  make sure to create this instance AFTER channel initialization in [McChannelManager]
 */
class ModServerEvents private constructor() {

    //? if fabric {
    init {
        ServerLifecycleEvents.SERVER_STARTING.register {
            ModServerLib.minecraftServer = it
        }
        ServerLifecycleEvents.SERVER_STARTED.register { fireServerStarted(it) }
        ServerLifecycleEvents.SERVER_STOPPING.register { fireServerStopping(it) }

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ -> firePlayerJoin(handler.player) }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ -> firePlayerQuit(handler.player) }

        S2CPlayChannelEvents.REGISTER.register(RegisterChannelHandler)

        //? if >=1.20.2 {
        /*S2CConfigurationChannelEvents.REGISTER.register(RegisterChannelHandler.ConfigHandler)
        *///?}

        //? if >=1.19 {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> fireRegisterCommands(dispatcher) }
        //?} else {
        /*CommandRegistrationCallback.EVENT.register { dispatcher, _ -> fireRegisterCommands(dispatcher) }
        *///?}
    }
    //?} else {
    
    /*//? if neoforge {
    /^init {
        NeoForge.EVENT_BUS.register(this)
    //? if >=1.20.2 {
        /^¹NeoForge.EVENT_BUS.register(RegisterChannelHandler)
    ¹^///?}
    }^///?} else {
    init {
        MinecraftForge.EVENT_BUS.register(this)
    //? if >=1.20.2 {
       /^MinecraftForge.EVENT_BUS.register(RegisterChannelHandler)
    ^///?}
    }//?}

    @SubscribeEvent
    //? if neoforge {
    /^fun onServerStart(event: net.neoforged.neoforge.event.server.ServerStartedEvent) {
    ^///?} else {
    fun onServerStart(event: net.minecraftforge.event.server.ServerStartedEvent) {
    //?}
        fireServerStarted(event.server)
    }

    @SubscribeEvent
    //? if neoforge {
    /^fun onServerStart(event: net.neoforged.neoforge.event.server.ServerStoppingEvent) {
    ^///?} else {
    fun onServerStart(event: net.minecraftforge.event.server.ServerStoppingEvent) {
    //?}
        fireServerStopping(event.server)
    }

    @SubscribeEvent
    fun onPlayerJoin(event: PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        firePlayerJoin(player)
    }

    @SubscribeEvent
    fun onPlayerQuit(event: PlayerLoggedOutEvent) {
        val player = event.entity as? ServerPlayer ?: return
        firePlayerQuit(player)
    }

    @SubscribeEvent
    fun onCommandRegister(event: RegisterCommandsEvent) {
        fireRegisterCommands(event.dispatcher)
    }
    *///?}

    private fun fireServerStarted(minecraftServer: MinecraftServer) {
        ModServerLib.onInitialize(minecraftServer)
        ServerStartedEvent.invoker.onServerStarted(minecraftServer)
    }

    private fun fireServerStopping(minecraftServer: MinecraftServer) {
        ServerStoppingEvent.invoker.onServerStopping(minecraftServer)
    }

    private fun fireRegisterCommands(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val minecraftServer = ModServerLib
        val commandManager = minecraftServer.commandManager

        commandManager.clear()
        McServerCommandsRegisterEvent.invoker.onCommandsRegister(commandManager, minecraftServer)
        commandManager.registerCommands(dispatcher)
    }

    private fun firePlayerJoin(player: ServerPlayer) {
        McPlayerJoinEvent.invoker.onPlayerJoin(player.toMcServerPlayer())
    }

    private fun firePlayerQuit(player: ServerPlayer) {
        McPlayerQuitEvent.invoker.onPlayerQuit(player.toMcServerPlayer())
    }

    companion object {

        private var instance: ModServerEvents? = null

        @JvmStatic
        fun initialize() {
            if (instance == null)
                instance = ModServerEvents()
        }
    }
}
