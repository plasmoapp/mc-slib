package su.plo.slib.mod.event

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import su.plo.slib.api.command.brigadier.McBrigadierRegistry
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent
import su.plo.slib.api.event.permission.McPermissionsRegisterEvent
import su.plo.slib.api.event.player.McPlayerJoinEvent
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.slib.mod.ModServerLib
import su.plo.slib.mod.channel.RegisterChannelHandler
import su.plo.slib.mod.event.server.ServerStartedEvent
import su.plo.slib.mod.event.server.ServerStoppingEvent
import su.plo.slib.mod.extension.toMcServerPlayer

//? if fabric {
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.S2CConfigurationChannelEvents
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
//?} else {
/*import net.neoforged.neoforge.common.NeoForge
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent
import su.plo.slib.mod.permission.NeoForgePermissions
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
            firePermissionsRegister()
        }
        ServerLifecycleEvents.SERVER_STARTED.register { fireServerStarted(it) }
        ServerLifecycleEvents.SERVER_STOPPING.register { fireServerStopping(it) }
        ServerLifecycleEvents.SERVER_STOPPED.register { ModServerLib.onServerStopped() }

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ -> firePlayerJoin(handler.player) }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ -> firePlayerQuit(handler.player) }

        S2CPlayChannelEvents.REGISTER.register(RegisterChannelHandler)

        S2CConfigurationChannelEvents.REGISTER.register(RegisterChannelHandler.ConfigHandler)

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> fireRegisterCommands(dispatcher) }
    }
    //?} else {
    /*init {
        NeoForge.EVENT_BUS.register(this)
        NeoForge.EVENT_BUS.register(RegisterChannelHandler)
    }

    @SubscribeEvent
    fun onServerAboutToStart(event: ServerAboutToStartEvent) {
        ModServerLib.minecraftServer = event.server
    }

    @SubscribeEvent
    fun onPermissionNodesGather(event: PermissionGatherEvent.Nodes) {
        firePermissionsRegister()
        NeoForgePermissions.registerNodes(
            event,
            ModServerLib.permissionManager,
            ModServerLib.permissionSupplier::getPermissionDefault,
        )
    }

    @SubscribeEvent
    fun onServerStart(event: net.neoforged.neoforge.event.server.ServerStartedEvent) {
        fireServerStarted(event.server)
    }

    @SubscribeEvent
    fun onServerStart(event: net.neoforged.neoforge.event.server.ServerStoppingEvent) {
        fireServerStopping(event.server)
    }

    @SubscribeEvent
    fun onServerStopped(event: net.neoforged.neoforge.event.server.ServerStoppedEvent) {
        ModServerLib.onServerStopped()
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

    private fun firePermissionsRegister() {
        McPermissionsRegisterEvent.invoker.onPermissionsRegister(ModServerLib.permissionManager)
    }

    private fun fireServerStopping(minecraftServer: MinecraftServer) {
        ServerStoppingEvent.invoker.onServerStopping(minecraftServer)
    }

    private fun fireRegisterCommands(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val minecraftServer = ModServerLib
        val commandManager = minecraftServer.commandManager

        val phase =
            if (minecraftServer.isBound) McBrigadierRegistry.Phase.RUNTIME
            else McBrigadierRegistry.Phase.BOOTSTRAP

        commandManager.clear()
        McServerCommandsRegisterEvent.invoker.onCommandsRegister(commandManager, minecraftServer)
        commandManager.registerCommands(dispatcher, phase)
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
