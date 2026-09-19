package su.plo.slib.mod.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >=1.21.9 {
/*import net.minecraft.server.players.NameAndId;
*///?} else {
import com.mojang.authlib.GameProfile;
//?}

//? if >=1.21.9 {
/*@Mixin(targets = "net.minecraft.server.players.CachedUserNameToIdResolver$GameProfileInfo")
*///?} else {
@Mixin(targets = "net.minecraft.server.players.GameProfileCache$GameProfileInfo")
//?}
public interface ProfileCacheEntryAccessor {
    //? if >=1.21.9 {
    /*@Accessor("nameAndId")
    NameAndId slib_getProfile();
    *///?} else {
    @Accessor("profile")
    GameProfile slib_getProfile();
    //?}
}
