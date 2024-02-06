package su.plo.slib.mod.mixin;

//#if FORGE
//#if MC<12002
//$$ import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
//$$ import net.minecraft.server.level.ServerPlayer;
//$$ import net.minecraft.server.network.ServerGamePacketListenerImpl;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.Shadow;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import su.plo.slib.mod.mixinkt.MixinServerGamePacketListenerImplKt;
//$$
//$$ @Mixin(ServerGamePacketListenerImpl.class)
//$$ public abstract class MixinServerGamePacketListenerImpl {
//$$
//$$     @Shadow public ServerPlayer player;
//$$
//$$     @Inject(method = "handleCustomPayload", at = @At("HEAD"))
//$$     public void handleCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
//$$         MixinServerGamePacketListenerImplKt.INSTANCE.handleCustomPayload(player, packet);
//$$     }
//$$ }
//#endif
//#endif
