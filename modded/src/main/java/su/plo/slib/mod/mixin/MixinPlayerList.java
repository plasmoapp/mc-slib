package su.plo.slib.mod.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.plo.slib.mod.mixinkt.MixinPlayerListKt;

//? if >=1.21 {
/*import net.minecraft.world.entity.Entity;
*///?}

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {

    @Inject(method = "respawn", at = @At("RETURN"))
    //? if >=1.21 {
    /*private void onRespawn(ServerPlayer serverPlayer, boolean bl, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir) {
    *///?} else {
    private void onRespawn(ServerPlayer serverPlayer, boolean bl, CallbackInfoReturnable<ServerPlayer> cir) {
    //?}
        ServerPlayer newPlayer = cir.getReturnValue();
        MixinPlayerListKt.INSTANCE.onRespawn(newPlayer);
    }
}
