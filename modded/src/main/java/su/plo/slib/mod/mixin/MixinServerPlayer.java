package su.plo.slib.mod.mixin;

import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.slib.mod.mixinkt.MixinServerPlayerKt;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer {
    @Inject(method = "updateOptions", at = @At("HEAD"))
    public void updateOptions(ClientInformation packet, CallbackInfo ci) {
        MixinServerPlayerKt.INSTANCE.updateOptions((ServerPlayer) (Object) this, packet);
    }
}
