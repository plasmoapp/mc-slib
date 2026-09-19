package su.plo.slib.mod.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

//? if >=1.21.9 {
/*import net.minecraft.server.players.CachedUserNameToIdResolver;
*///?} else {
import net.minecraft.server.players.GameProfileCache;
//?}

//? if >=1.21.9 {
/*@Mixin(CachedUserNameToIdResolver.class)
*///?} else {
@Mixin(GameProfileCache.class)
//?}
public interface ProfileCacheAccessor {
    @Accessor("profilesByName")
    Map<String, ?> slib_getProfilesByName();
}
